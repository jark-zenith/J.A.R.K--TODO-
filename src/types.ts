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
