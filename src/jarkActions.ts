import { taskService } from './taskService';
import type { JarkAction, Task, TaskDraft, TaskPriority, TaskStatus } from './types';

const priorities: TaskPriority[] = ['low', 'medium', 'high', 'urgent'];
const statuses: TaskStatus[] = ['todo', 'in-progress', 'completed'];

export type JarkExecutionResult = {
  tasks: Task[];
  reply: string;
  changed: boolean;
};

export type JarkExecutionOptions = {
  confirmDelete?: (task: Task) => boolean;
};

function localDate(offset = 0): string {
  const date = new Date();
  date.setHours(12, 0, 0, 0);
  date.setDate(date.getDate() + offset);
  return date.toISOString().slice(0, 10);
}

function normalizeDueDate(value: string | undefined): string {
  if (!value) return '';
  const normalized = value.trim().toLowerCase();
  if (normalized === 'today') return localDate();
  if (normalized === 'tomorrow') return localDate(1);
  const days = normalized.match(/^in (\d+) days?$/);
  if (days) return localDate(Number(days[1]));
  if (/^\d{4}-\d{2}-\d{2}$/.test(normalized) && !Number.isNaN(new Date(`${normalized}T12:00:00`).getTime())) return normalized;
  throw new Error(`I could not understand the deadline “${value}”.`);
}

function validateAction(action: JarkAction): void {
  if (!action || typeof action !== 'object' || typeof action.action !== 'string') throw new Error('J.A.R.K. returned an invalid action.');
  if (action.priority && !priorities.includes(action.priority)) throw new Error('J.A.R.K. returned an invalid priority.');
  if (action.status && !statuses.includes(action.status)) throw new Error('J.A.R.K. returned an invalid status.');
  if (action.tags && (!Array.isArray(action.tags) || action.tags.some((tag) => typeof tag !== 'string'))) throw new Error('J.A.R.K. returned invalid tags.');
  if (action.dueDate) normalizeDueDate(action.dueDate);
}

function resolveTask(tasks: Task[], action: JarkAction): Task {
  if (action.taskId) {
    const task = tasks.find((item) => item.id === action.taskId);
    if (!task) throw new Error('I could not find that task.');
    return task;
  }
  const reference = (action.query || action.title || '').trim();
  if (!reference) throw new Error('Tell me which task you mean.');
  const matches = taskService.searchTasks(tasks, reference);
  if (!matches.length) throw new Error(`I could not find a task matching “${reference}”.`);
  if (matches.length > 1) throw new Error(`I found ${matches.length} tasks matching “${reference}”. Please be more specific.`);
  return matches[0];
}

function taskListReply(label: string, tasks: Task[]): string {
  if (!tasks.length) return `You have no ${label} tasks.`;
  return `You have ${tasks.length} ${label} ${tasks.length === 1 ? 'task' : 'tasks'}: ${tasks.map((task) => task.title).join('; ')}.`;
}

export function executeJarkAction(input: unknown, tasks: Task[], options: JarkExecutionOptions = {}): JarkExecutionResult {
  const action = input as JarkAction;
  validateAction(action);
  switch (action.action) {
    case 'create_task': {
      const title = action.title?.trim();
      if (!title) throw new Error('I need a task title before I can create it.');
      const draft: TaskDraft = {
        title,
        description: action.description?.trim() || '',
        priority: action.priority || 'medium',
        category: action.category?.trim() || 'Personal',
        dueDate: normalizeDueDate(action.dueDate),
        tags: action.tags || []
      };
      const task = taskService.createTask(draft);
      return { tasks: [task, ...tasks], changed: true, reply: `Done. I added “${task.title}”${task.dueDate ? ` for ${task.dueDate}` : ''}.` };
    }
    case 'update_task': {
      const task = resolveTask(tasks, action);
      const changes: Partial<Task> = {};
      if (action.title?.trim()) changes.title = action.title.trim();
      if (action.description !== undefined) changes.description = action.description;
      if (action.priority) changes.priority = action.priority;
      if (action.category?.trim()) changes.category = action.category.trim();
      if (action.dueDate !== undefined) changes.dueDate = normalizeDueDate(action.dueDate);
      if (action.status) changes.status = action.status;
      if (action.tags) changes.tags = action.tags;
      if (!Object.keys(changes).length) throw new Error('Tell me what should change on that task.');
      const updated = taskService.updateTask(task, changes);
      return { tasks: tasks.map((item) => item.id === task.id ? updated : item), changed: true, reply: `Done. I updated “${updated.title}”.` };
    }
    case 'complete_task': {
      const task = resolveTask(tasks, action);
      const completed = task.status === 'completed' ? task : taskService.updateTask(task, { status: 'completed' });
      return { tasks: tasks.map((item) => item.id === task.id ? completed : item), changed: task.status !== 'completed', reply: completed === task ? `“${task.title}” is already complete.` : `Done. “${task.title}” is now complete.` };
    }
    case 'delete_task': {
      const task = resolveTask(tasks, action);
      if (options.confirmDelete && !options.confirmDelete(task)) return { tasks, changed: false, reply: `I left “${task.title}” unchanged.`, };
      return { tasks: taskService.deleteTask(tasks, task.id), changed: true, reply: `Done. I deleted “${task.title}”.` };
    }
    case 'get_tasks':
      return { tasks, changed: false, reply: taskListReply('open', tasks.filter((task) => task.status !== 'completed')) };
    case 'search_tasks': {
      const matches = taskService.searchTasks(tasks, action.query || action.title || '');
      return { tasks, changed: false, reply: taskListReply('matching', matches) };
    }
    case 'get_today_tasks':
      return { tasks, changed: false, reply: taskListReply('due today', taskService.getTodayTasks(tasks, localDate())) };
    case 'get_upcoming_tasks':
      return { tasks, changed: false, reply: taskListReply('upcoming', taskService.getUpcomingTasks(tasks, localDate())) };
    case 'get_overdue_tasks':
      return { tasks, changed: false, reply: taskListReply('overdue', taskService.getOverdueTasks(tasks, localDate())) };
    case 'none':
      return { tasks, changed: false, reply: 'I can help manage your tasks. Tell me what you would like to create, update, complete, delete, or find.' };
    default:
      throw new Error('J.A.R.K. returned an unsupported action.');
  }
}