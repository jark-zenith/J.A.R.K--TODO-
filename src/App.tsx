import { FormEvent, useMemo, useState } from 'react';
import {
  Archive, Bell, CalendarDays, Check, ChevronDown, Circle, Clock3, Command, Filter, LayoutDashboard,
  ListTodo, Menu, Mic, MoreHorizontal, Plus, Search, Settings2, Sparkles, Tag, Trash2, X, Zap
} from 'lucide-react';
import { taskService } from './taskService';
import type { Task, TaskDraft, TaskPriority, TaskStatus } from './types';

type View = 'dashboard' | 'all' | 'today' | 'upcoming' | 'overdue' | 'completed' | 'priority';
type ModalState = { mode: 'create' | 'edit'; task?: Task } | null;

const categories = ['Personal', 'Study', 'Development', 'Business', 'Projects', 'Other'];
const priorityOrder: TaskPriority[] = ['urgent', 'high', 'medium', 'low'];
const emptyDraft: TaskDraft = { title: '', description: '', priority: 'medium', category: 'Personal', dueDate: '', tags: [] };

function formatDueDate(date: string): string {
  if (!date) return 'No deadline';
  const today = new Date().toISOString().slice(0, 10);
  if (date === today) return 'Today';
  return new Intl.DateTimeFormat('en', { month: 'short', day: 'numeric' }).format(new Date(`${date}T12:00:00`));
}

function App() {
  const [tasks, setTasks] = useState<Task[]>(() => taskService.getTasks());
  const [view, setView] = useState<View>('dashboard');
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | TaskStatus>('all');
  const [priorityFilter, setPriorityFilter] = useState<'all' | TaskPriority>('all');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [modal, setModal] = useState<ModalState>(null);
  const [mobileNav, setMobileNav] = useState(false);
  const [notice, setNotice] = useState('');
  const [command, setCommand] = useState('');
  const [commandStatus, setCommandStatus] = useState<'idle' | 'loading' | 'listening'>('idle');
  const [commandReply, setCommandReply] = useState('');

  const today = new Date().toISOString().slice(0, 10);
  const todayTasks = useMemo(() => taskService.getTodayTasks(tasks, today), [tasks, today]);
  const upcomingTasks = useMemo(() => taskService.getUpcomingTasks(tasks, today), [tasks, today]);
  const overdueTasks = useMemo(() => taskService.getOverdueTasks(tasks, today), [tasks, today]);
  const priorityTasks = useMemo(() => taskService.getPriorityTasks(tasks), [tasks]);
  const completedTasks = useMemo(() => tasks.filter((task) => task.status === 'completed'), [tasks]);

  const visibleTasks = useMemo(() => {
    let result = view === 'today' ? todayTasks : view === 'upcoming' ? upcomingTasks : view === 'overdue' ? overdueTasks : view === 'completed' ? completedTasks : view === 'priority' ? priorityTasks : tasks;
    result = taskService.searchTasks(result, query);
    if (statusFilter !== 'all') result = result.filter((task) => task.status === statusFilter);
    if (priorityFilter !== 'all') result = result.filter((task) => task.priority === priorityFilter);
    if (categoryFilter !== 'all') result = result.filter((task) => task.category === categoryFilter);
    return result;
  }, [categoryFilter, completedTasks, overdueTasks, priorityFilter, priorityTasks, query, statusFilter, tasks, todayTasks, upcomingTasks, view]);

  const completionRate = tasks.length ? Math.round((completedTasks.length / tasks.length) * 100) : 0;
  const persist = (nextTasks: Task[]) => { setTasks(nextTasks); try { taskService.saveTasks(nextTasks); } catch { setNotice('Your browser blocked local storage. Changes will last for this session.'); } };
  const updateTask = (taskId: string, changes: Partial<Task>) => persist(tasks.map((task) => task.id === taskId ? taskService.updateTask(task, changes) : task));
  const toggleTask = (task: Task) => persist(tasks.map((item) => item.id === task.id ? taskService.completeTask(item) : item));
  const removeTask = (task: Task) => { if (window.confirm(`Delete “${task.title}”?`)) persist(taskService.deleteTask(tasks, task.id)); };
  const submitTask = (draft: TaskDraft) => { persist(modal?.task ? tasks.map((task) => task.id === modal.task!.id ? taskService.updateTask(task, draft) : task) : [taskService.createTask(draft), ...tasks]); setModal(null); };
  const navigate = (nextView: View) => { setView(nextView); setMobileNav(false); };
  const submitCommand = async () => {
    if (!command.trim() || commandStatus === 'loading') return;
    setCommandStatus('loading'); setCommandReply('');
    try {
      const response = await fetch('/api/command', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ command }) });
      const result = await response.json() as { reply?: string; error?: string };
      if (!response.ok) throw new Error(result.error || 'J.A.R.K. could not process that instruction.');
      setCommandReply(result.reply || 'Instruction received.');
    } catch (error) { setCommandReply(error instanceof Error ? error.message : 'J.A.R.K. could not process that instruction.'); }
    finally { setCommandStatus('idle'); }
  };
  const toggleVoice = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) { setCommandReply('Voice input is not supported in this browser. Try Chrome or Edge.'); return; }
    const recognition = new SpeechRecognition();
    recognition.lang = 'en-US'; recognition.interimResults = false; recognition.maxAlternatives = 1;
    recognition.onstart = () => setCommandStatus('listening');
    recognition.onresult = (event) => { setCommand(event.results[0][0].transcript); setCommandStatus('idle'); };
    recognition.onerror = () => { setCommandReply('I could not hear that instruction. Please try again.'); setCommandStatus('idle'); };
    recognition.onend = () => setCommandStatus('idle');
    recognition.start();
  };

  return <div className="app-shell">
    <aside className={`sidebar ${mobileNav ? 'sidebar-open' : ''}`}>
      <div className="brand"><span className="brand-mark">J</span><div><strong>J.A.R.K.</strong><small>TO-DO SYSTEM</small></div></div>
      <nav aria-label="Main navigation">
        <NavItem icon={<LayoutDashboard size={17} />} label="Dashboard" active={view === 'dashboard'} count={tasks.length} onClick={() => navigate('dashboard')} />
        <p className="nav-label">Workspace</p>
        <NavItem icon={<ListTodo size={17} />} label="All tasks" active={view === 'all'} count={tasks.filter((task) => task.status !== 'completed').length} onClick={() => navigate('all')} />
        <NavItem icon={<CalendarDays size={17} />} label="Today" active={view === 'today'} count={todayTasks.length} onClick={() => navigate('today')} />
        <NavItem icon={<Clock3 size={17} />} label="Upcoming" active={view === 'upcoming'} count={upcomingTasks.length} onClick={() => navigate('upcoming')} />
        <NavItem icon={<Bell size={17} />} label="Overdue" active={view === 'overdue'} count={overdueTasks.length} onClick={() => navigate('overdue')} />
        <NavItem icon={<Archive size={17} />} label="Completed" active={view === 'completed'} count={completedTasks.length} onClick={() => navigate('completed')} />
        <NavItem icon={<Zap size={17} />} label="High priority" active={view === 'priority'} count={priorityTasks.length} onClick={() => navigate('priority')} />
        <p className="nav-label categories-label">Categories</p>
        {categories.slice(0, 4).map((category) => <button className="category-link" key={category} onClick={() => { setCategoryFilter(category); navigate('all'); }}><span className={`category-dot ${category.toLowerCase()}`} />{category}<span>{tasks.filter((task) => task.category === category && task.status !== 'completed').length}</span></button>)}
      </nav>
      <div className="sidebar-bottom"><button className="add-task-button" onClick={() => setModal({ mode: 'create' })}><Plus size={18} /> Add task <kbd>N</kbd></button><button className="settings-button"><Settings2 size={16} /> Settings</button></div>
    </aside>
    {mobileNav && <button className="nav-backdrop" aria-label="Close navigation" onClick={() => setMobileNav(false)} />}
    <main className="main-content">
      <header className="topbar"><button className="icon-button mobile-menu" aria-label="Open navigation" onClick={() => setMobileNav(true)}><Menu size={20} /></button><div className="breadcrumb"><span>J.A.R.K. /</span><strong>{view === 'dashboard' ? 'Command center' : view}</strong></div><div className="topbar-actions"><label className="global-search"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search tasks..." aria-label="Search tasks" /><kbd>⌘ K</kbd></label><button className="icon-button" aria-label="Notifications"><Bell size={18} /></button><div className="avatar">JD</div></div></header>
      <section className="content-wrap">
        {notice && <div className="notice" role="status">{notice}<button onClick={() => setNotice('')} aria-label="Dismiss"><X size={15} /></button></div>}
        {view === 'dashboard' ? <Dashboard tasks={tasks} todayTasks={todayTasks} overdueTasks={overdueTasks} completionRate={completionRate} onNavigate={navigate} onToggle={toggleTask} onEdit={(task) => setModal({ mode: 'edit', task })} onCreate={() => setModal({ mode: 'create' })} command={command} commandStatus={commandStatus} commandReply={commandReply} onCommandChange={setCommand} onCommandSubmit={submitCommand} onVoice={toggleVoice} /> : <TaskView title={view === 'all' ? 'All tasks' : view === 'priority' ? 'High priority' : view[0].toUpperCase() + view.slice(1)} subtitle={`${visibleTasks.length} ${visibleTasks.length === 1 ? 'task' : 'tasks'} in this view`} tasks={visibleTasks} query={query} statusFilter={statusFilter} priorityFilter={priorityFilter} categoryFilter={categoryFilter} setStatusFilter={setStatusFilter} setPriorityFilter={setPriorityFilter} setCategoryFilter={setCategoryFilter} onToggle={toggleTask} onEdit={(task) => setModal({ mode: 'edit', task })} onDelete={removeTask} onCreate={() => setModal({ mode: 'create' })} />}
      </section>
    </main>
    {modal && <TaskModal mode={modal.mode} task={modal.task} onClose={() => setModal(null)} onSubmit={submitTask} />}
  </div>;
}

function NavItem({ icon, label, count, active, onClick }: { icon: React.ReactNode; label: string; count: number; active: boolean; onClick: () => void }) { return <button className={`nav-item ${active ? 'active' : ''}`} onClick={onClick}>{icon}<span>{label}</span><b>{count}</b></button>; }
function Dashboard({ tasks, todayTasks, overdueTasks, completionRate, onNavigate, onToggle, onEdit, onCreate, command, commandStatus, commandReply, onCommandChange, onCommandSubmit, onVoice }: { tasks: Task[]; todayTasks: Task[]; overdueTasks: Task[]; completionRate: number; onNavigate: (view: View) => void; onToggle: (task: Task) => void; onEdit: (task: Task) => void; onCreate: () => void; command: string; commandStatus: 'idle' | 'loading' | 'listening'; commandReply: string; onCommandChange: (value: string) => void; onCommandSubmit: () => void; onVoice: () => void }) {
  const focusTasks = [...todayTasks, ...overdueTasks].sort((a, b) => priorityOrder.indexOf(a.priority) - priorityOrder.indexOf(b.priority));
  return <><div className="page-heading"><div><p className="eyebrow"><span className="status-pulse" /> PERSONAL OPERATING SYSTEM</p><h1>Good morning, Jordan<span className="accent">.</span></h1><p className="subheading">Here’s the signal for your day. Keep the important work moving.</p></div><button className="primary-button" onClick={onCreate}><Plus size={17} /> New task</button></div><div className="stats-grid"><StatCard label="Open tasks" value={tasks.filter((task) => task.status !== 'completed').length} detail="Across your workspace" icon={<ListTodo />} tone="blue" /><StatCard label="Completed" value={tasks.filter((task) => task.status === 'completed').length} detail={`${completionRate}% completion rate`} icon={<Check />} tone="green" /><StatCard label="Due today" value={todayTasks.length} detail={overdueTasks.length ? `${overdueTasks.length} need attention` : 'You are on track'} icon={<CalendarDays />} tone="amber" /><StatCard label="Focus score" value={`${Math.min(100, completionRate + 38)}%`} detail="Based on recent momentum" icon={<Sparkles />} tone="red" /></div><div className="dashboard-grid"><section className="panel focus-panel"><div className="panel-heading"><div><p className="eyebrow">PRIORITY QUEUE</p><h2>What should I work on?</h2></div><button className="text-button" onClick={() => onNavigate('priority')}>View all <ChevronDown size={14} /></button></div>{focusTasks.length ? <div className="task-stack">{focusTasks.slice(0, 4).map((task) => <TaskRow key={task.id} task={task} onToggle={onToggle} onEdit={onEdit} />)}</div> : <EmptyState compact onCreate={onCreate} />}</section><section className="panel progress-panel"><div className="panel-heading"><div><p className="eyebrow">SYSTEM HEALTH</p><h2>Momentum</h2></div><Sparkles className="panel-icon" size={19} /></div><div className="ring-wrap"><div className="progress-ring" style={{ '--progress': `${completionRate * 3.6}deg` } as React.CSSProperties}><div><strong>{completionRate}%</strong><span>complete</span></div></div></div><div className="momentum-copy"><strong>{completionRate >= 60 ? 'Strong momentum' : 'Build your rhythm'}</strong><span>{completionRate >= 60 ? 'Your consistency is paying off.' : 'Finish one focused task to shift the day.'}</span></div><div className="mini-metrics"><span><b>{todayTasks.length}</b> due today</span><span><b>{tasks.filter((task) => task.status === 'in-progress').length}</b> in motion</span></div></section></div><section className="command-panel"><div className="command-icon"><Command size={21} /></div><div className="command-copy"><p className="eyebrow">J.A.R.K. COMMAND LAYER</p><h2>Ask J.A.R.K. anything<span className="accent">.</span></h2><p>Turn intent into action when you’re ready. Your command interface is standing by.</p></div><form className="command-input" onSubmit={(event) => { event.preventDefault(); onCommandSubmit(); }}><input value={command} onChange={(event) => onCommandChange(event.target.value)} placeholder="What should I work on next?" aria-label="Ask J.A.R.K." disabled={commandStatus === 'loading'} /><button type="button" aria-label="Voice input" onClick={onVoice} className={commandStatus === 'listening' ? 'is-listening' : ''}><Mic size={17} /></button><button type="submit" aria-label="Send command" disabled={commandStatus === 'loading' || !command.trim()}><ChevronDown size={17} className="send-icon" /></button></form>{commandReply && <p className="command-reply" role="status">{commandReply}</p>}</section></>;
}
function StatCard({ label, value, detail, icon, tone }: { label: string; value: string | number; detail: string; icon: React.ReactNode; tone: string }) { return <div className="stat-card"><div className={`stat-icon ${tone}`}>{icon}</div><span className="stat-label">{label}</span><strong className="stat-value">{value}</strong><span className="stat-detail">{detail}</span></div>; }
function TaskView({ title, subtitle, tasks, query, statusFilter, priorityFilter, categoryFilter, setStatusFilter, setPriorityFilter, setCategoryFilter, onToggle, onEdit, onDelete, onCreate }: { title: string; subtitle: string; tasks: Task[]; query: string; statusFilter: 'all' | TaskStatus; priorityFilter: 'all' | TaskPriority; categoryFilter: string; setStatusFilter: (value: 'all' | TaskStatus) => void; setPriorityFilter: (value: 'all' | TaskPriority) => void; setCategoryFilter: (value: string) => void; onToggle: (task: Task) => void; onEdit: (task: Task) => void; onDelete: (task: Task) => void; onCreate: () => void }) { return <><div className="page-heading compact-heading"><div><p className="eyebrow">TASK REGISTRY / {title.toUpperCase()}</p><h1>{title}<span className="accent">.</span></h1><p className="subheading">{subtitle}{query ? ` matching “${query}”` : ''}</p></div><button className="primary-button" onClick={onCreate}><Plus size={17} /> New task</button></div><div className="filter-bar"><Filter size={15} /><select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as 'all' | TaskStatus)} aria-label="Filter by status"><option value="all">All status</option><option value="todo">To do</option><option value="in-progress">In progress</option><option value="completed">Completed</option></select><select value={priorityFilter} onChange={(event) => setPriorityFilter(event.target.value as 'all' | TaskPriority)} aria-label="Filter by priority"><option value="all">All priority</option>{priorityOrder.map((priority) => <option key={priority} value={priority}>{priority[0].toUpperCase() + priority.slice(1)}</option>)}</select><select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)} aria-label="Filter by category"><option value="all">All categories</option>{categories.map((category) => <option key={category}>{category}</option>)}</select></div><section className="panel task-list-panel">{tasks.length ? tasks.map((task) => <TaskRow key={task.id} task={task} onToggle={onToggle} onEdit={onEdit} onDelete={onDelete} />) : <EmptyState onCreate={onCreate} />}</section></>; }
function TaskRow({ task, onToggle, onEdit, onDelete }: { task: Task; onToggle: (task: Task) => void; onEdit: (task: Task) => void; onDelete?: (task: Task) => void }) { return <article className={`task-row ${task.status === 'completed' ? 'is-complete' : ''}`}><button className={`task-check ${task.status === 'completed' ? 'checked' : ''}`} onClick={() => onToggle(task)} aria-label={`${task.status === 'completed' ? 'Reopen' : 'Complete'} ${task.title}`}>{task.status === 'completed' ? <Check size={15} /> : <Circle size={17} />}</button><div className="task-main" onClick={() => onEdit(task)}><div className="task-title-line"><h3>{task.title}</h3><span className={`priority-badge ${task.priority}`}>{task.priority}</span></div><div className="task-meta"><span>{task.category}</span>{task.dueDate && <><i /> <span className={task.dueDate < new Date().toISOString().slice(0, 10) && task.status !== 'completed' ? 'overdue-text' : ''}><CalendarDays size={13} /> {formatDueDate(task.dueDate)}</span></>}{task.tags.length > 0 && <><i /> <span><Tag size={12} /> {task.tags.join(', ')}</span></>}</div></div><button className="row-action" onClick={() => onEdit(task)} aria-label={`Edit ${task.title}`}><MoreHorizontal size={18} /></button>{onDelete && <button className="row-action delete-action" onClick={() => onDelete(task)} aria-label={`Delete ${task.title}`}><Trash2 size={16} /></button>}</article>; }
function EmptyState({ onCreate, compact = false }: { onCreate: () => void; compact?: boolean }) { return <div className={`empty-state ${compact ? 'compact' : ''}`}><div className="empty-symbol"><ListTodo size={20} /></div><strong>{compact ? 'No urgent signals' : 'Nothing here yet'}</strong><span>{compact ? 'Your priority queue is clear.' : 'Create a task to populate this view.'}</span>{!compact && <button className="text-button" onClick={onCreate}>Create a task <Plus size={14} /></button>}</div>; }
function TaskModal({ mode, task, onClose, onSubmit }: { mode: 'create' | 'edit'; task?: Task; onClose: () => void; onSubmit: (draft: TaskDraft) => void }) { const [draft, setDraft] = useState<TaskDraft>(task ? { title: task.title, description: task.description, priority: task.priority, category: task.category, dueDate: task.dueDate, tags: task.tags } : emptyDraft); const [error, setError] = useState(''); const submit = (event: FormEvent) => { event.preventDefault(); if (!draft.title.trim()) { setError('A task title is required.'); return; } if (draft.dueDate && Number.isNaN(new Date(`${draft.dueDate}T12:00:00`).getTime())) { setError('Enter a valid deadline.'); return; } onSubmit({ ...draft, title: draft.title.trim(), tags: draft.tags.map((tag) => tag.trim()).filter(Boolean) }); }; return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}><div className="modal" role="dialog" aria-modal="true" aria-labelledby="task-modal-title"><div className="modal-heading"><div><p className="eyebrow">TASK REGISTRY</p><h2 id="task-modal-title">{mode === 'create' ? 'Create a task' : 'Edit task'}</h2></div><button className="icon-button" onClick={onClose} aria-label="Close"><X size={18} /></button></div><form onSubmit={submit}><label>Title <input autoFocus value={draft.title} onChange={(event) => setDraft({ ...draft, title: event.target.value })} placeholder="What needs your attention?" /></label><label>Description <textarea value={draft.description} onChange={(event) => setDraft({ ...draft, description: event.target.value })} placeholder="Add useful context..." rows={3} /></label><div className="form-grid"><label>Priority <select value={draft.priority} onChange={(event) => setDraft({ ...draft, priority: event.target.value as TaskPriority })}>{priorityOrder.map((priority) => <option key={priority} value={priority}>{priority[0].toUpperCase() + priority.slice(1)}</option>)}</select></label><label>Category <select value={draft.category} onChange={(event) => setDraft({ ...draft, category: event.target.value })}>{categories.map((category) => <option key={category}>{category}</option>)}</select></label></div><div className="form-grid"><label>Deadline <input type="date" value={draft.dueDate} onChange={(event) => setDraft({ ...draft, dueDate: event.target.value })} /></label><label>Tags <input value={draft.tags.join(', ')} onChange={(event) => setDraft({ ...draft, tags: event.target.value.split(',') })} placeholder="design, focus" /></label></div>{error && <p className="form-error" role="alert">{error}</p>}<div className="modal-actions"><button type="button" className="secondary-button" onClick={onClose}>Cancel</button><button type="submit" className="primary-button">{mode === 'create' ? 'Create task' : 'Save changes'}</button></div></form></div></div>; }

export default App;
export { App };
