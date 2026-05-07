# Agent Instructions

## 1. Task & Knowledge Tracking (`beads`)
- **Workflow:** `bd prime` (context) ➔ `bd ready` (find) ➔ `bd show <id>` (view) ➔ `bd update <id> --claim` ➔ `bd close <id>`
- **Strict Rule:** Use ONLY `bd` for tasks (NO markdown TODOs) and `bd remember` for knowledge (NO MEMORY.md).

## 2. Context & Discovery (`claude-mem` & `graphify`)
*Context MUST precede raw file reading/grepping.*
- **Memory:** `claude-mem` (`search` ➔ `timeline` ➔ `get_observations`).
- **Architecture:** Read `graphify-out/GRAPH_REPORT.md` or use `graphify query/path/explain`.
- **Symbols:** Discover via `claude-mem` (smart_* tools) or `graphify query`.
- **Maintenance:** Run `graphify update .` after ANY code change.
- **FORBIDDEN:** Broad recursive folder scans or logic/symbol greps. Grep ONLY for exact literals (errors, paths).

## 3. Execution Constraints
- **Non-Interactive Shell:** ALWAYS prevent hangs. Force flags are mandatory: `-f` (`cp -f`, `mv -f`, `rm -rf`), `-y` (`apt-get -y`), and `-o BatchMode=yes` (`scp/ssh`).

## 4. Session Completion
*Session is incomplete until a remote push succeeds.*
1. **Wrap-up:** File pending issues, run quality gates (test/lint/build), and update `bd` status (close/in-progress).

## Agent skills
### Issue tracker
`bd` (beads) CLI. See `docs/agents/issue-tracker.md`.
### Triage labels
Default vocabulary. See `docs/agents/triage-labels.md`.
### Domain docs
Single-context. See `docs/agents/domain.md`.