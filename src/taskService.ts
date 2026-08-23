import type { Task, TaskDraft, TaskPriority, TaskStatus } from './types';

const STORAGE_KEY = 'jark-tasks-v1';

const starterTasks: Task[] = [
  {
    id: 'starter-1', title: 'Map the next J.A.R.K. milestone', description: 'Turn the product vision into three shippable outcomes.', status: 'in-progress', priority: 'high', category: 'Projects', dueDate: new Date().toISOString().slice(0, 10), createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), completedAt: null, tags: ['planning', 'jark']
  },
  {
    id: 'starter-2', title: 'Study TypeScript patterns', description: 'Review discriminated unions and service boundaries.', status: 'todo', priority: 'medium', category: 'Study', dueDate: new Date(Date.now() + 86400000).toISOString().slice(0, 10), createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), completedAt: null, tags: ['learning']
  },
  {
    id: 'starter-3', title: 'Archive completed notes', description: '', status: 'completed', priority: 'low', category: 'Personal', dueDate: '', createdAt: new Date(Date.now() - 86400000).toISOString(), updatedAt: new Date().toISOString(), completedAt: new Date().toISOString(), tags: []
  }
];

function isTask(value: unknown): value is Task {
  if (!value || typeof value !== 'object') return false;
  const task = value as Partial<Task>;
  return typeof task.id === 'string' && typeof task.title === 'string' && typeof task.status === 'string' && typeof task.priority === 'string' && Array.isArray(task.tags);
}

export const taskService = {
  getTasks(): Task[] {
    try {
      const stored = window.localStorage.getItem(STORAGE_KEY);
      if (!stored) return starterTasks;
      const parsed: unknown = JSON.parse(stored);
      return Array.isArray(parsed) && parsed.every(isTask) ? parsed : starterTasks;
    } catch {
      return starterTasks;
    }
  },
  saveTasks(tasks: Task[]): void {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
  },
  createTask(draft: TaskDraft): Task {
    const now = new Date().toISOString();
    return { ...draft, id: crypto.randomUUID(), status: 'todo', createdAt: now, updatedAt: now, completedAt: null };
  },
  updateTask(task: Task, changes: Partial<Task>): Task {
    const next = { ...task, ...changes, updatedAt: new Date().toISOString() };
    if (next.status === 'completed' && !next.completedAt) next.completedAt = new Date().toISOString();
    if (next.status !== 'completed') next.completedAt = null;
    return next;
  },
  completeTask(task: Task): Task {
    return this.updateTask(task, { status: task.status === 'completed' ? 'todo' : 'completed' });
  },
  deleteTask(tasks: Task[], taskId: string): Task[] {
    return tasks.filter((task) => task.id !== taskId);
  },
  searchTasks(tasks: Task[], query: string): Task[] {
    const loweredQuery = query.trim().toLowerCase();
    if (!loweredQuery) return tasks;
    return tasks.filter((task) => [task.title, task.description, task.category, ...task.tags].join(' ').toLowerCase().includes(loweredQuery));
  },
  getTodayTasks(tasks: Task[], today = new Date().toISOString().slice(0, 10)): Task[] {
    return tasks.filter((task) => task.dueDate === today && task.status !== 'completed');
  },
  getUpcomingTasks(tasks: Task[], today = new Date().toISOString().slice(0, 10)): Task[] {
    return tasks.filter((task) => task.dueDate > today && task.status !== 'completed');
  },
  getOverdueTasks(tasks: Task[], today = new Date().toISOString().slice(0, 10)): Task[] {
    return tasks.filter((task) => task.dueDate && task.dueDate < today && task.status !== 'completed');
  },
  getPriorityTasks(tasks: Task[], priorities: TaskPriority[] = ['high', 'urgent']): Task[] {
    return tasks.filter((task) => priorities.includes(task.priority) && task.status !== 'completed');
  },
  getStatusLabel(status: TaskStatus): string {
    return status === 'in-progress' ? 'In progress' : status === 'completed' ? 'Completed' : 'To do';
  }
};
