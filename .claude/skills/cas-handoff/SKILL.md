---
name: cas-handoff
description: "Finish and hand off a change in the CAS repository: Checkstyle on changed Java files, no stale .git/index.lock, release-notes and puppeteer cross-checks, and the ready-to-use commit message. Use at the end of every task that changed files in cas-server, before reporting the work as done."
---


# Handing off CAS work

Run these steps, in order, before reporting the work as done. The rules behind them follow.

1. Compile what changed, main and test sources, with Error Prone, each changed module on its own class path so a
   missing test dependency shows (see Conventions in AGENTS.md, the `cas-testing` skill and, in a sandbox without
   Gradle, the `cas-sandbox-verification` skill).
2. Run Checkstyle on every changed Java file (rule below), and fix overloads, imports and file length it reports.
3. Run `git --no-optional-locks diff --check`.
4. Cross-check the puppeteer scenarios that cover the change and update them (`cas-puppeteer-scenarios` skill).
5. Add the release-notes entry when the work calls for one (security notes: see "Test, concurrency, and hand-off
   expectations" in AGENTS.md; release-notes format: `cas-docs-site` skill).
6. Record new, durable lessons in AGENTS.md or in the matching skill under `.claude/skills/`.
7. Check `ls .git/*.lock` and clear what is left.
8. End with the commit message (rule below). Say clearly what was not run.

## Commit message

- When the work is done, end the hand-off with a ready-to-use commit message covering only the uncommitted changes
  (`git --no-optional-locks status`): a conventional subject (`fix:`, `feat:`, ... as in `git log`) and a short body.
  The maintainer commits; do not commit yourself.

## Git locks and Checkstyle

- Leave no `.git/index.lock` behind. `git status` and `git diff` take that lock to refresh the index, and in a remote environment that cannot delete files the lock survives the command and blocks every subsequent git operation the maintainer runs. Read git state with `git --no-optional-locks status` / `git --no-optional-locks diff`, which never takes it, and before finishing check `ls .git/*.lock` and clear anything left. If deletion is refused, request it rather than leaving the repository wedged.
- Maintainer: run Checkstyle on every changed Java file before handing work off, even without Gradle. Use the version in
  `gradle/libs.versions.toml` (`checkstyle-<version>-all.jar` from the Checkstyle GitHub releases; 14.x needs Java 21+) as
  `java -Dcheckstyle.suppressions.file=style/checkstyle-suppressions.xml -Dcheckstyle.importcontrol.file=style/import-control.xml
  -jar checkstyle.jar -c style/checkstyle-rules.xml <files>`. When the workspace JDK is older, tar the files and `style/` into
  `build/` (deletable without asking) and run it where Java 21 is available.
