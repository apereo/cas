---
name: cas-import-pr
description: "Bring an apereo/cas GitHub pull request onto a local branch without a git identity, keeping the contributor as author, by fetching each non-merge commit as a patch and applying it with git am. Use when asked to review, fix up or test a contributor PR locally."
---


# Importing a contributor pull request

1. List the PR's commits (`https://api.github.com/repos/apereo/cas/pulls/<n>/commits`) and drop the
   "Merge branch 'master'" commits.
2. Cut a local branch from master.
3. For each remaining commit, oldest first, fetch the patch and apply it as described below.
4. Leave the result uncommitted beyond the contributor's own commits; the maintainer commits review fixes himself
   (`cas-handoff` skill).

## Applying the patches

- The remote shell has no git identity. To bring a contributor's PR onto a local branch, fetch each non-merge commit as a patch from `https://api.github.com/repos/apereo/cas/commits/<sha>` with `Accept: application/vnd.github.patch` and apply it with `git -c user.name=... -c user.email=... am --3way`, which keeps the contributor as author. Skip the "Merge branch 'master'" commits; the local branch is cut from master already.
