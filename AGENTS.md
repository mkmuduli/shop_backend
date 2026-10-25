# Repository rules

## Secrets

- Never open, read, print, summarize, copy, or include the contents of `.env` or any `.env.*` file in model or chat context.
- Do not run commands that may expose those contents, including `cat`, `grep`, `printenv` on secret values, or commands that dump application configuration. Do not use `.env` values in prompts, logs, reports, or generated files.
- If a task appears to require a secret, ask the user to provide a safe alternative, such as a placeholder or a runtime-only configuration method. Continue with unrelated work without inspecting the secret file.
- `.env.example` may contain placeholder names and non-secret sample values only; never put real credentials in it.

These rules are repository instructions. They do not technically prevent a model or process with workspace access from reading files. Keep real secret files outside the workspace and provide secrets to the application only at runtime.
