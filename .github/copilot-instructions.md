# Copilot Instructions — Homesteader (Steady Hand)

Livestock management app for homesteaders and small-scale ranchers. Kotlin Multiplatform targeting Android and iOS.

## Long-Term Memory (SimpleMem)

You have access to a **SimpleMem** MCP server via the `simplemem` tool. Use it to maintain persistent memory across sessions for this project.

### When to store memories
- Architecture decisions and the reasoning behind them
- Project conventions (naming, patterns, file structure)
- Known bugs, gotchas, or constraints
- User preferences and recurring feedback
- Task progress, TODOs, and open questions
- KMP/platform-specific implementation details
- Key dependencies and their versions

### When to retrieve memories
- At the start of any session or new task — query for relevant context
- Before making architectural decisions — check if a prior decision exists
- When something feels familiar — check memory before re-investigating

### How to use the tools
- `memory_add` — store a single dialogue or fact
- `memory_add_batch` — store multiple facts at once
- `memory_query` — ask a natural language question ("What SQLDelight schema changes have been made?")
- `memory_retrieve` — browse raw stored facts
- `memory_stats` — check memory status
- `memory_clear` — clear all memories (use with caution)

### Example usage
At the start of a session:
> Use `memory_query` with "What is the current state of this project and any open tasks?"

After a significant decision:
> Use `memory_add` to record what was decided and why.
