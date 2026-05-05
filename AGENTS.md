# Agent Instructions

## Issue Tracking (beads)

```bash
bd prime                # Full workflow context
bd ready                # Find available work
bd show <id>            # View issue details
bd update <id> --claim  # Claim work
bd close <id>           # Complete work
```

- Use `bd` for ALL task tracking — NOT TodoWrite/TaskCreate/markdown TODOs
- Use `bd remember` for persistent knowledge — NOT MEMORY.md files

## Non-Interactive Shell

**ALWAYS use `-f` flags** to prevent interactive hang:

```bash
cp -f / mv -f / rm -f          # Force overwrite
rm -rf / cp -rf                 # Recursive force
scp -o BatchMode=yes            # Non-interactive scp/ssh
apt-get -y                      # Auto-confirm
```

## Session Completion

Work is NOT complete until `git push` succeeds. **NEVER** stop before pushing.

1. File issues for remaining work
2. Run quality gates (tests/linters/builds) if code changed
3. Update issue status — close finished, update in-progress
4. **PUSH TO REMOTE:**
   ```bash
   git pull --rebase && bd dolt push && git push
   git status  # MUST show "up to date with origin"
   ```
5. Clean up stashes, prune remote branches
6. Hand off context for next session

## Context & Discovery (claude-mem & graphify)

Before reading raw files or grepping, **MUST build context first:**

1. **Memory (claude-mem):** `search(query)` → `timeline(anchor=ID)` → `get_observations([IDs])`
2. **Architecture (graphify):** Read `graphify-out/GRAPH_REPORT.md` or use `graphify query/path/explain`
3. **Symbol Discovery:** Use `claude-mem smart_search/smart_outline/smart_unfold` or `graphify query` — **always before** Glob/Grep/Read
4. **Maintenance:** Run `graphify update .` after ANY code change

**Forbidden:** recursive folder scans, broad Glob/Grep sweeps for logic/symbol lookups. Grep only for exact strings (error messages, log literals, file paths).
