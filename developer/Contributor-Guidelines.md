---
layout: default
title: CAS - Contributor Guidelines
---
# Contributor Guidelines

## Sign the Contributor License Agreement
Very important, before we can accept your code into Jasig Central Authentication Service, we will need you to sign the Jasig contributor license agreement (CLA). Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do. Active contributors might be asked to join the core team, and given the ability to merge pull requests. In order to read and sign the CLA, please go to:

* https://wiki.jasig.org/display/LIC/Jasig+Licensing+Policy

## Fork the Code Base
1. Go to https://github.com/Jasig/CAS
2. Hit the "fork" button and choose your own github account as the target
3. See http://help.github.com/fork-a-repo/ for more information

## Setup Development Environment
1. `git clone --recursive git@github.com:<your-github-username>/cas.git`
2. `cd cas`
3. `git remote show` you should see only 'origin' - which is the fork you created for your own github account
4. `git remote add upstream git@github.com:Jasig/cas.git`
5. `git remote show` you should now see 'upstream' in addition to 'origin' where 'upstream' is the Jasig repository from which releases are built
6. `git fetch --all`
7. `git branch -a you should see branches on origin as well as upstream, including 'master'`

## A Day in the Life of a Contributor
* Always work on topic branches.
* For example, to create and switch to a new branch for issue CAS-123: `git checkout -b CAS-123`
* You might be working on several different topic branches at any given time, but when at a stopping point for one of those branches, commit (a local operation).
* Please follow the "Commit Guidelines" described in this chapter of Pro Git: http://progit.org/book/ch5-2.html
* Then to begin working on another issue (say CAS-101): `git checkout CAS-101`. The -b flag is not needed if that branch already exists in your local repository.
* When ready to resolve an issue or to collaborate with others, you can push your branch to origin (your fork), e.g.: `git push origin CAS-123`
* If you want to collaborate with another contributor, have them fork your repository (add it as a remote) and `git fetch <your-username>` to grab your branch. Alternatively, they can use `git fetch --all` to sync their local state with all of their remotes.
* If you grant that collaborator push access to your repository, they can even apply their changes to your branch.
When ready for your contribution to be reviewed for potential inclusion in the master branch of the canonical cas repository (what you know as 'upstream'), issue a pull request to the Jasig CAS repository (for more detail, see http://help.github.com/send-pull-requests/).
* The project lead may merge your changes into the upstream master branch as-is, he may keep the pull request open yet add a comment about something that should be modified, or he might reject the pull request by closing it.
A prerequisite for any pull request is that it will be cleanly merge-able with the upstream master's current state. *This is the responsibility of any contributor.* If your pull request cannot be applied cleanly, the project lead will most likely add a comment requesting that you make it merge-able. For a full explanation, see the Pro Git section on rebasing: http://progit.org/book/ch3-6.html. As stated there: "> Often, you’ll do this to make sure your commits apply cleanly on a remote branch — perhaps in a project to which you’re trying to contribute but that you don’t maintain."

## Keeping your Local Code in Sync
* As mentioned above, you should always work on topic branches (since 'master' is a moving target). However, you do want to always keep your own 'origin' master branch in synch with the 'upstream' master.
* Within your local working directory, you can sync up all remotes' branches with: `git fetch --all`
* While on your own local master branch: `git pull upstream master` (which is the equivalent of fetching upstream/master and merging that into the branch you are in currently)
* Now that you're in synch, switch to the topic branch where you plan to work, e.g.: `git checkout -b CAS-123`
* When you get to a stopping point: `git commit`
* If changes have occurred on the upstream/master while you were working you can synch again:
     - Switch back to master: `git checkout master`
     - Then: `git pull upstream master`
     - Switch back to the topic branch: git checkout CAS-123 (no -b needed since the branch already exists)
     - Rebase the topic branch to minimize the distance between it and your recently synched master branch: git rebase master
(Again, for more detail see the Pro Git section on rebasing: http://progit.org/book/ch3-6.html)
* *Note* You cannot rebase if you have already pushed your branch to your remote because you'd be rewriting history (see *'The Perils of Rebasing'* in the article). If you rebase by mistake, you can undo it as discussed [in this stackoverflow discussion](http://stackoverflow.com/questions/134882/undoing-a-git-rebase). Once you have published your branch, you need to merge in the master rather than rebasing.
* Now, if you issue a pull request, it is much more likely to be merged without conflicts. Most likely, any pull request that would produce conflicts will be deferred until the issuer of that pull request makes these adjustments.
* Assuming your pull request is merged into the 'upstream' master, you will actually end up pulling that change into your own master eventually, and at that time, you may decide to delete the topic branch from your local repository and your fork (origin) if you pushed it there.
     - To delete the local branch: `git branch -d CAS-123`
     - To delete the branch from your origin: `git push origin :CAS-123`

## What Should be in a Pull Request (Everyone)
* The [JIRA issue number](https://issues.jasig.org/browse/CAS) - there should be no pull request without a corresponding JIRA tracking number.
* The core of the contribution itself (code, documentation, etc.)
* Unit tests (where applicable)
* Updated documentation (where applicable)

*Note: even core contributors with commit access should follow the above Contributor Guidelines*

## Dealing with a Pull Request (Committers only)
* Ensure the request has the above information above
* Review the request for code quality, formatting, and overall project conformance
* Suggest changes where required
* Pull in additional code reviewers if needed (i.e. if major architectural change)
* Ideally, pull requests should be reviewed by a member of a different organization to ensure shared/mutual understanding across organizations (the assumption being there was some internal discussion already).
* Update the related Jira issue with a link to the pull request and/or GitHub code review.