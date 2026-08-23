# J.A.R.K. To-Do — VS Code Agent Project Specification

## Mission

Build a production-quality MVP of **J.A.R.K. To-Do** in this repository. This is a real application, not a static mockup. It will later become a task-management module of the wider J.A.R.K. AI ecosystem.

## Before coding

1. Inspect the repository and existing files.
2. Preserve useful existing infrastructure.
3. Choose a modern TypeScript-compatible React stack if no stack exists; prefer Vite + React + TypeScript + Tailwind CSS + shadcn/ui where appropriate.
4. Keep dependencies minimal.
5. Create a clean, maintainable architecture.
6. Never expose API keys in frontend source code.

## MVP functionality

Implement:

- Create, read, update, and delete tasks.
- Mark tasks complete and reopen them.
- Task fields: id, title, description, status, priority, category, dueDate, createdAt, updatedAt, completedAt, tags.
- Statuses: todo, in-progress, completed.
- Priorities: low, medium, high, urgent.
- Dashboard with total tasks, completed tasks, overdue tasks, today's tasks, and completion percentage.
- Views for All, Today, Upcoming, Overdue, Completed, and High Priority.
- Search across title, description, category, and tags.
- Filters for status, priority, category, and due date.
- Default categories: Personal, Study, Development, Business, Projects, Other.
- Persistent browser storage for the MVP.
- Dedicated data/task service layer so persistence can later be replaced by an API/database.
- Responsive desktop, tablet, and mobile UI.
- Accessible controls and keyboard-friendly interaction.

## J.A.R.K. visual identity

Create a premium futuristic dark-first productivity interface. Use restrained blue technology accents, controlled red accents, neutral/silver surfaces, subtle gradients, thin borders, soft glows, modern typography, and tasteful micro-interactions. Avoid excessive neon, gaming-dashboard styling, visual clutter, and animation overload.

The interface should feel like a serious J.A.R.K. technology product rather than a generic tutorial application.

## AI-ready architecture

Create a task service with operations conceptually equivalent to:

- createTask()
- updateTask()
- deleteTask()
- completeTask()
- getTasks()
- searchTasks()
- getTodayTasks()
- getUpcomingTasks()
- getOverdueTasks()

Add a J.A.R.K. command-bar UI such as `Ask J.A.R.K...`. Do not pretend an AI API is connected if no backend/API is configured. The interface should be ready to accept future text, voice, and structured AI actions.

Future flow:

User command -> J.A.R.K. model -> intent/action -> Task Service -> persistence/API -> UI

Example future action:

```json
{
  "action": "create_task",
  "title": "Study JavaScript",
  "priority": "high",
  "category": "Study"
}
```

## Voice readiness

J.A.R.K. is intended to support voice interaction in the broader ecosystem. Keep the command interface architecturally independent from its input source so it can later accept typed text, microphone input, or AI-generated commands. Do not add complex voice infrastructure unless it is already available and stable.

## Architecture guidance

Prefer reusable components similar to:

```text
src/
  components/
    layout/
    dashboard/
    tasks/
    ai/
    ui/
  services/
  store/
  types/
  lib/
```

Adapt to the selected framework and do not create unnecessary abstraction.

Keep task manipulation logic out of presentation components. Avoid `any` unless unavoidable.

## Security

Never commit `.env`, API keys, passwords, tokens, or private credentials. Use `.env.example` for variable names only. Any future AI provider integration must use a server-side/API boundary for secrets.

## Quality requirements

- No fake buttons.
- No dead controls.
- No hardcoded data presented as the permanent data layer.
- Strong TypeScript types.
- Proper validation.
- Useful error states.
- Loading states where applicable.
- Empty states for task views.
- Clean responsive behavior.
- Reasonable performance.

## Implementation sequence

1. Inspect and initialize the application.
2. Establish types and task data layer.
3. Implement persistence.
4. Implement task CRUD.
5. Build dashboard and task views.
6. Add search/filtering.
7. Build the J.A.R.K. visual system.
8. Add the J.A.R.K. command bar.
9. Add AI integration interfaces without inventing an API connection.
10. Test all core flows and fix build/type/lint errors.

## Verification checklist

Before considering the work complete, verify:

- `npm install` succeeds.
- Development server starts.
- Production build succeeds.
- Creating a task works.
- Editing works.
- Completion/reopening works.
- Deletion works.
- Search works.
- Filters work.
- Refresh preserves tasks.
- Today's/upcoming/overdue views behave correctly.
- Mobile layout is usable.
- No secrets are committed.

## Git workflow

Make focused commits after meaningful milestones. Use messages such as:

- `feat: initialize J.A.R.K. To-Do app`
- `feat: add task CRUD and persistence`
- `feat: add dashboard and task views`
- `feat: add search and filters`
- `feat: add J.A.R.K. command interface`
- `fix: resolve task persistence issue`

Do not force-push or rewrite repository history unless explicitly instructed.
