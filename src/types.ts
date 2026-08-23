export type TaskStatus = 'todo' | 'in-progress' | 'completed';
export type TaskPriority = 'low' | 'medium' | 'high' | 'urgent';

export interface Task {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  category: string;
  dueDate: string;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
  tags: string[];
  projectId?: string;
  aiGenerated?: boolean;
}

export type TaskDraft = Pick<Task, 'title' | 'description' | 'priority' | 'category' | 'dueDate' | 'tags'>;

export type JarkActionName = 'create_task' | 'update_task' | 'delete_task' | 'complete_task' | 'get_tasks' | 'search_tasks' | 'get_today_tasks' | 'get_upcoming_tasks' | 'get_overdue_tasks' | 'none';

export interface JarkAction {
  action: JarkActionName;
  taskId?: string;
  title?: string;
  description?: string;
  priority?: TaskPriority;
  category?: string;
  dueDate?: string;
  status?: TaskStatus;
  tags?: string[];
  query?: string;
}
