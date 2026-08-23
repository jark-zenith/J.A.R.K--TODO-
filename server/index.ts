import { createServer } from 'node:http';
import { URL } from 'node:url';

const port = Number(process.env.PORT ?? 8787);
const openAiKey = process.env.OPENAI_API_KEY;
const model = process.env.OPENAI_MODEL ?? 'gpt-4o-mini';

type CommandAction = {
  action: 'create_task' | 'update_task' | 'delete_task' | 'complete_task' | 'get_tasks' | 'search_tasks' | 'get_today_tasks' | 'get_upcoming_tasks' | 'get_overdue_tasks' | 'none';
  title?: string;
  taskId?: string;
  query?: string;
  priority?: 'low' | 'medium' | 'high' | 'urgent';
  category?: string;
  dueDate?: string;
  description?: string;
  status?: 'todo' | 'in-progress' | 'completed';
  tags?: string[];
};

type CommandResponse = { reply: string; action: CommandAction };
const actionNames = new Set<CommandAction['action']>(['create_task', 'update_task', 'delete_task', 'complete_task', 'get_tasks', 'search_tasks', 'get_today_tasks', 'get_upcoming_tasks', 'get_overdue_tasks', 'none']);

function isCommandResponse(value: unknown): value is CommandResponse {
  if (!value || typeof value !== 'object') return false;
  const response = value as Partial<CommandResponse>;
  if (typeof response.reply !== 'string' || !response.action || typeof response.action !== 'object') return false;
  const action = response.action as Partial<CommandAction>;
  return typeof action.action === 'string' && actionNames.has(action.action as CommandAction['action']);
}

function sendJson(response: import('node:http').ServerResponse, status: number, body: unknown): void {
  response.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' });
  response.end(JSON.stringify(body));
}

async function readBody(request: import('node:http').IncomingMessage): Promise<unknown> {
  let body = '';
  for await (const chunk of request) body += chunk;
  if (body.length > 20_000) throw new Error('Request is too large.');
  return JSON.parse(body);
}

async function handleCommand(request: import('node:http').IncomingMessage, response: import('node:http').ServerResponse): Promise<void> {
  if (!openAiKey) {
    sendJson(response, 503, { error: 'J.A.R.K. AI is not configured. Add OPENAI_API_KEY to .env.' });
    return;
  }

  const body = await readBody(request) as { command?: unknown };
  if (typeof body.command !== 'string' || !body.command.trim()) {
    sendJson(response, 400, { error: 'A non-empty command is required.' });
    return;
  }

  const openAiResponse = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: { Authorization: `Bearer ${openAiKey}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({
      model,
      temperature: 0.2,
      response_format: { type: 'json_object' },
      messages: [
        { role: 'system', content: 'You are J.A.R.K., a concise task-management intent parser. Return JSON only with keys reply and action. action must be an object with action set to one of create_task, update_task, delete_task, complete_task, get_tasks, search_tasks, get_today_tasks, get_upcoming_tasks, get_overdue_tasks, none. Use title for a new task title. For operations on an existing task, use query for the words that identify it and never invent a taskId. For update_task include only requested changes: title, description, priority, category, dueDate, status, or tags. For create_task include priority, category, dueDate, and tags when known. Dates may be today, tomorrow, in N days, or YYYY-MM-DD. Use none when the request is conversational or cannot be mapped safely. The reply is only a brief intent acknowledgement; the application will generate the final response from real task data.' },
        { role: 'user', content: body.command.trim() }
      ]
    })
  });

  if (!openAiResponse.ok) {
    sendJson(response, 502, { error: 'The J.A.R.K. AI provider returned an error.' });
    return;
  }

  const payload = await openAiResponse.json() as { choices?: Array<{ message?: { content?: string } }> };
  const content = payload.choices?.[0]?.message?.content;
  if (!content) throw new Error('The AI provider returned an empty response.');
  const parsed: unknown = JSON.parse(content);
  if (!isCommandResponse(parsed)) {
    sendJson(response, 502, { error: 'J.A.R.K. returned an invalid action.' });
    return;
  }
  const result = parsed;
  sendJson(response, 200, result);
}

const server = createServer(async (request, response) => {
  const requestUrl = new URL(request.url ?? '/', `http://${request.headers.host ?? 'localhost'}`);
  if (requestUrl.pathname === '/api/health' && request.method === 'GET') {
    sendJson(response, 200, { ok: true, aiConfigured: Boolean(openAiKey) });
    return;
  }
  if (requestUrl.pathname === '/api/command' && request.method === 'POST') {
    try { await handleCommand(request, response); } catch { sendJson(response, 400, { error: 'Unable to process that command.' }); }
    return;
  }
  sendJson(response, 404, { error: 'Route not found.' });
});

server.listen(port, () => console.log(`J.A.R.K. API listening on http://localhost:${port}`));