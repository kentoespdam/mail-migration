# Agent Instructions

This project uses **bd** (beads) for issue tracking. Run `bd prime` for full workflow context.

## Quick Reference

```bash
bd ready              # Find available work
bd show <id>          # View issue details
bd update <id> --claim  # Claim work atomically
bd close <id>         # Complete work
bd dolt push          # Push beads data to remote
```

## Non-Interactive Shell Commands

**ALWAYS use non-interactive flags** with file operations to avoid hanging on confirmation prompts.

Shell commands like `cp`, `mv`, and `rm` may be aliased to include `-i` (interactive) mode on some systems, causing the agent to hang indefinitely waiting for y/n input.

**Use these forms instead:**
```bash
# Force overwrite without prompting
cp -f source dest           # NOT: cp source dest
mv -f source dest           # NOT: mv source dest
rm -f file                  # NOT: rm file

# For recursive operations
rm -rf directory            # NOT: rm -r directory
cp -rf source dest          # NOT: cp -r source dest
```

**Other commands that may prompt:**
- `scp` - use `-o BatchMode=yes` for non-interactive
- `ssh` - use `-o BatchMode=yes` to fail instead of prompting
- `apt-get` - use `-y` flag
- `brew` - use `HOMEBREW_NO_AUTO_UPDATE=1` env var

<!-- BEGIN BEADS INTEGRATION v:1 profile:minimal hash:ca08a54f -->
## Beads Issue Tracker

This project uses **bd (beads)** for issue tracking. Run `bd prime` to see full workflow context and commands.

### Quick Reference

```bash
bd ready              # Find available work
bd show <id>          # View issue details
bd update <id> --claim  # Claim work
bd close <id>         # Complete work
```

### Rules

- Use `bd` for ALL task tracking — do NOT use TodoWrite, TaskCreate, or markdown TODO lists
- Run `bd prime` for detailed command reference and session close protocol
- Use `bd remember` for persistent knowledge — do NOT use MEMORY.md files

## Session Completion

**When ending a work session**, you MUST complete ALL steps below. Work is NOT complete until `git push` succeeds.

**MANDATORY WORKFLOW:**

1. **File issues for remaining work** - Create issues for anything that needs follow-up
2. **Run quality gates** (if code changed) - Tests, linters, builds
3. **Update issue status** - Close finished work, update in-progress items
4. **PUSH TO REMOTE** - This is MANDATORY:
   ```bash
   git pull --rebase
   bd dolt push
   git push
   git status  # MUST show "up to date with origin"
   ```
5. **Clean up** - Clear stashes, prune remote branches
6. **Verify** - All changes committed AND pushed
7. **Hand off** - Provide context for next session

**CRITICAL RULES:**
- Work is NOT complete until `git push` succeeds
- NEVER stop before pushing - that leaves work stranded locally
- NEVER say "ready to push when you are" - YOU must push
- If push fails, resolve and retry until it succeeds
<!-- END BEADS INTEGRATION -->

## MANDATORY: Context & Discovery (claude-mem & graphify)

Before reading raw files or grepping for logic, you MUST build context:

1. **Memory First (claude-mem):** Check for past decisions, bugfixes, or patterns using the 3-layer workflow:
   - `search(query)` → Get index with IDs.
   - `timeline(anchor=ID)` → Get surrounding context.
   - `get_observations([IDs])` → Fetch full details ONLY for filtered IDs.
2. **Architecture First (graphify):** Read `graphify-out/GRAPH_REPORT.md` (or `graphify-out/wiki/index.md`) to understand the god nodes and community structure.
3. **Semantic Lookup & Symbol Discovery:**
   - Use `graphify query`, `graphify path`, or `graphify explain` for conceptual or cross-module questions.
   - Use `claude-mem smart_search` / `smart_outline` / `smart_unfold` for structural code lookup (functions, classes, methods) to avoid reading full files.
   - **Always use these tools to locate source-code symbols** before reaching for Glob/Grep/Read.
4. **Maintenance:** Run `graphify update .` after ANY code change.

**Forbidden**: recursive folder scanning, broad Glob/Grep sweeps for conceptual/logic questions or symbol lookups. Use Grep only for exact-string textual lookups (a known error message, log literal, or file-path string).
