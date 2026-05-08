# Agent Instructions

## 1. Task & Knowledge Tracking (`beads`)
- **Workflow:** `bd prime` (context) ➔ `bd ready` (find) ➔ `bd show <id>` (view) ➔ `bd update <id> --claim` ➔ `bd close <id>`
- **Rule:** Use ONLY `bd` for tasks (NO markdown TODOs). Use `bd remember` for persistent knowledge.

## 2. Discovery & Documentation (`graphify` & `context7`)
*Context MUST precede raw file reading/grepping.*
- **Internal Arch:** Use `graphify query/path/explain` or read `graphify-out/GRAPH_REPORT.md`.
- **External Libs:** Use `context7` MCP/CLI for ANY library/framework docs (React, Spring, etc.).
- **Memory:** Use `claude-mem` search tools for cross-session context.
- **Maintenance:** Run `graphify update .` after ANY code change.
- **Forbidden:** Broad recursive folder scans. Use discovery tools first.

## 3. Execution Constraints
- **Shell:** ALWAYS use force flags (`-f`, `-y`, `BatchMode=yes`) to prevent interactive hangs.
- **Coding:** Adhere to `CLAUDE.md` for build/test and `memory.md` for domain/stack specs.

## 4. Session Completion
1. **Wrap-up:** File pending issues in `bd`, run tests/lint.
2. **Commit:** Propose concise commit message.
3. **Push:** Session is incomplete until a remote push succeeds.

## Agent Skills
- **Issue Tracker:** `bd` (beads) CLI. See `docs/agents/issue-tracker.md`.
- **Triage Labels:** See `docs/agents/triage-labels.md`.
- **Domain Docs:** See `docs/agents/domain.md`.
