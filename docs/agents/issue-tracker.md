# Issue tracker: Beads (bd)
Track tasks via `bd` CLI as defined in `AGENTS.md`.
## Workflow
- `bd prime`: Load context.
- `bd ready`: Find tasks.
- `bd show <id>`: View task.
- `bd update <id> --claim`: Claim task.
- `bd close <id>`: Complete task.
- `bd remember`: Record knowledge.
- Sync: `bd dolt push` + `git push`.
## Actions
- Publish: Use `bd` commands or `bd remember`.
- Fetch: Use `bd show <id>` or `bd ready`.
