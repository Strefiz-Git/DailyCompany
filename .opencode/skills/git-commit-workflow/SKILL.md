---
name: git-commit-workflow
description: MANDATORY git workflow for every coding task - inspect repository state before starting, create a branch for experimental, large or risky changes, make small atomic commits with Conventional Commit messages that include a descriptive body, never commit secrets or broken builds, and never run destructive git commands. Load this skill at the START of every task that will modify files and follow its commit rules at the END of every task, even for small changes, and whenever the user mentions git, commit, branch, merge, revert, undo, history, or "save my progress".
license: MIT
compatibility: opencode
metadata:
  scope: all-tasks
  mandatory: "true"
---

# Git workflow (mandatory)

This skill applies to **every task that changes files**. Do not skip it because the change is small.

**Commit message language:** English (Conventional Commits). To use Portuguese (PT-PT) instead, the user can change this line to `Commit message language: pt-PT`; if it says that, write the subject and body in European Portuguese but keep the type keywords (`feat`, `fix`, ...) in English.

Commit message language: English

## 1. Before starting a task

Run and read the output:

```bash
git status --short --branch
git log --oneline -5
```

1. **Not a git repository?** Tell the user and offer `git init`, a proper Android `.gitignore` (see section 8) and an initial commit. Do nothing else in git until they agree.
2. **Uncommitted changes that you did not make?** Do not mix them with your work. Stop and ask the user whether to commit them first (as a separate commit), keep them, or stash them. Never stash, discard or overwrite them silently.
3. **Decide the branch** with the table in section 2. Create it *before* editing any file.
4. Check the current branch. Never do non-trivial work directly on `main` / `master`.

## 2. When to create a branch

Create a new branch **before changing code** when the change is any of these:

| Situation | Branch prefix | Example |
|---|---|---|
| New feature or screen | `feat/` | `feat/today-screen` |
| Bug fix that touches more than one file or needs investigation | `fix/` | `fix/alarm-not-firing-after-reboot` |
| Experimental idea, spike, "let's try", uncertain outcome | `exp/` | `exp/glance-widget` |
| Large refactor, architecture change, package renames | `refactor/` | `refactor/repository-layer` |
| Dependency, Gradle, AGP, Kotlin, SDK or library upgrades | `chore/` | `chore/update-compose-bom` |
| Database schema change or migration | `feat/` or `fix/` (mention "db" in the name) | `feat/db-recurring-events` |
| Documentation only | `docs/` | `docs/readme-setup` |

Also branch when **any** of these is true: the change is expected to touch **more than about 5 files or 200 lines**, it could break the build for a while, or you are not sure it will work.

**Project convention (this repo, `AGENTS.md` §5):** use the prefixes `feat/`, `fix/`, `exp/`, `chore/`, `docs/`. Do **not** use `feature/`, `experiment/` or `refactor/` branch names in this repo (existing branches already use `feat/`).

**Trivial changes** (typo, comment, a one-line obvious fix, a single string) may be committed directly on the current branch **if it is not `main`/`master`**. On `main`/`master`, ask the user or create a small `fix/` branch.

Branch rules:
- Names: lowercase, kebab-case, short, descriptive, prefix as above.
- Branch off the up-to-date base branch (usually `main`). If the current branch already matches the task, continue on it.
- One task per branch. If the user changes topic, commit what is done and switch/create another branch.
- **Never delete a branch** and never merge into `main` without the user's explicit OK.

```bash
git switch -c feat/today-screen
```

## 3. Commit early, in small atomic commits

- **One logical change per commit.** Do not bundle unrelated things (for example a refactor plus a new feature).
- Commit after each working step (a compiling, coherent unit), not only at the end. Long tasks should produce several commits.
- Stage explicit paths, then review what will be committed:

```bash
git add path/to/File1.kt path/to/File2.kt
git diff --staged --stat
git diff --staged
```

Avoid blind `git add .` / `git add -A`. If you use it, first check `git status` and make sure no unwanted files (build output, keys, local config) are included.

## 4. Commit message format (Conventional Commits)

```
<type>(<scope>): <summary in imperative mood, max 72 chars, no final period>

<body: what changed and WHY, wrapped at ~72 chars. Mention notable decisions,
trade-offs, and anything the reviewer should know.>

<footer: BREAKING CHANGE, Refs, Closes>
```

Types: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `style`, `build` (Gradle/dependencies), `ci`, `chore`, `revert`. On experimental branches `wip` is allowed for checkpoints, but the final state should be squashed or cleaned only if the user asks.

Scopes (use what matches the project): `ui`, `today`, `tasks`, `notes`, `calendar`, `alarms`, `wardrobe`, `data`, `db`, `network`, `nav`, `theme`, `gradle`, `deps`.

**A body is required** for every commit except truly trivial ones (typo, formatting). A summary-only commit for a feature or fix is not acceptable.

Example:

```
feat(alarms): reschedule reminders after device reboot

Alarms set with AlarmManager are cleared when the device restarts, so
reminders silently stopped firing. Add a BOOT_COMPLETED receiver that
loads all active reminders from Room and schedules the next occurrence
of each one.

Also reschedule on TIMEZONE_CHANGED and MY_PACKAGE_REPLACED.
```

Pass multi-line messages with several `-m` flags (works in PowerShell, bash and cmd):

```bash
git commit -m "feat(alarms): reschedule reminders after device reboot" \
           -m "Alarms set with AlarmManager are cleared on restart, so reminders stopped firing. Add a BOOT_COMPLETED receiver that reloads active reminders from Room and schedules the next occurrence."
```

(On PowerShell put everything on one line or use the backtick `` ` `` for line continuation instead of `\`.)

Bad messages (never write these): `update`, `fix`, `changes`, `wip stuff`, `final`, `asdf`, `fix bug`.

## 5. Before every commit (checklist)

- [ ] The project **builds**: `./gradlew :app:assembleDebug` (and unit tests if logic changed: `./gradlew :app:testDebugUnitTest`).
- [ ] `git diff --staged` reviewed: only intended changes; no debug prints, commented-out junk or TODO noise you added.
- [ ] **No secrets or private files**: keystores (`*.jks`, `*.keystore`), `keystore.properties`, `local.properties`, API keys, tokens, `google-services.json` unless the user confirms it is safe for this repo.
- [ ] No generated/build files (`build/`, `.gradle/`, `*.apk`, `*.aab`) staged.
- [ ] Message follows section 4.

Do **not** commit a broken build to a shared/main branch. On an `exp/` or `wip` branch a non-compiling checkpoint is allowed only if the message starts with `wip:` and the final report says so.
Never use `--no-verify` to bypass hooks.

## 6. Experimental or risky work

1. Create `exp/<name>` before touching code.
2. Commit checkpoints as you go (`wip:` is fine here). Before a risky step (large rename, migration, dependency jump), make a commit first so the previous state is always recoverable.
3. If the experiment fails, **do not delete it**. Report the outcome and let the user decide; suggest `git switch main` to go back.
4. If it succeeds, summarize the result and let the user decide how to merge (see section 7).

## 7. Merging, pushing, history

- **Do not merge, rebase, push, tag or delete branches unless the user explicitly asks.** When work is finished, propose the exact commands and wait.
- Suggested merge (after user OK): `git switch main`, `git merge --no-ff feat/today-screen`, or squash if the user prefers a clean history.
- When asked to push: `git push -u origin <branch>`; never `--force`. If a force push seems necessary, stop and explain why; use `--force-with-lease` only if the user explicitly approves.
- To undo something already shared or committed: use `git revert <hash>` (creates a new commit). `git commit --amend` is allowed **only** for the latest commit you created in this session and only if it was not pushed.
- Never rewrite history that was pushed.

## 8. Forbidden without explicit user approval

`git reset --hard`, `git clean -f/-fd/-fdx`, `git checkout -- .` / `git restore .` (discards changes), `git push --force`, `git branch -D`, `git rebase` (especially interactive), `git filter-branch`, `git stash drop/clear`, deleting `.git`, editing the git config globally.

If one of these seems needed, explain why, show what would be lost, and ask first.

## 9. Android `.gitignore` essentials

Make sure these are ignored (add only if missing, and commit that change alone as `chore(git): ...`):

```
*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build/
/captures/
.externalNativeBuild/
.cxx/
*.apk
*.aab
*.jks
*.keystore
keystore.properties
```

If a secret was already committed, tell the user immediately: it must be revoked/rotated, and removing it from history needs their decision.

## 10. End-of-task report (always)

At the end of every task, output:

- Branch name (and whether it was newly created)
- List of commits: short hash + subject (`git log --oneline -n <count>`)
- Build/test commands run and results
- Anything left **uncommitted**, and why
- Suggested next step (for example the merge command), without executing it

Example final check: run `git status --short` and confirm the working tree is clean, or explain what remains.
