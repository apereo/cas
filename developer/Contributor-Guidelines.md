---
layout: default
title: CAS - Contributor Guidelines
---

# Contributor Guidelines

## Getting Started
All CAS contributions SHOULD be made via GitHub pull request, which requires that contributions are offered from
a fork of the Apereo CAS repository:

[https://github.com/apereo/CAS](https://github.com/apereo/CAS)

Refer to the GitHub [Fork a Repo](http://help.github.com/fork-a-repo/) page for help with forking.

The following shell commands may be used to grab the source from a forked repository:

```bash
git clone --recursive git@github.com:$USER/cas.git
cd cas
git remote add upstream git@github.com:apereo/cas.git
git fetch --all
```

We encourage reading [Pro Git](http://git-scm.com/book/) prior to beginning development if you are unfamiliar with Git.


## Development Process
All CAS contributions SHOULD be submitted via GitHub pull requests. The following guidelines facilitate pull requests
that have a high likelihood of acceptance:

* Work on topic branches.
* Follow the [Pro Git commit guidelines](http://git-scm.com/book/ch5-2.html#Commit-Guidelines).


### Development Walk-Through

#### 1. Create Topic Branch

    git checkout -b CAS-123


#### 2. Edit Source and Commit
Edit source files and commit in logical chunks. We encourage numerous small commits over one large commit. Small,
focused commits facilitate review and will be more likely to be accepted. It is vital to summarize changes with
succinct commit messages. You SHOULD follow the
[Pro Git commit guidelines](http://git-scm.com/book/ch5-2.html#Commit-Guidelines) generally, but the following
commit message provides a good model:

	CAS-1238 Refactor principal resolver components.

	1. Rename CredentialToPrincipalResolver -> PrincipalResolver.
	2. Leverage symmetry between Credential and Principal ID to create a
	   generic principal resolution strategy, BasicPrincipalResolver.
	3. Make Person Directory resolver a concrete class that uses generic
	   principal ID resolution strategy by default and add capability to set
	   principal ID with an attribute value from Person Directory query results.
	4. Delete deprecated and superfluous resolver components.
	5. Rename some X.509 resolver components for clarity/consistency.

<p/>


#### 3. Push to Forked Repository
You must push your local branch to your forked repository to facilitate a pull request.

    git push origin CAS-123


#### 4. Submit Pull Request
Submit a pull request from your topic branch onto the target branch of CAS, typically _master_. See the GitHub
[Using Pull Requests](https://help.github.com/articles/using-pull-requests) page for help.

Be prepared to sync changes with the target branch of the CAS repository since the target branch may move during review and consideration of the pull request.


#### Creating Pull Requests
A pull request should contain the following:

* One or more commits that follow the [Pro Git commit guidelines](http://git-scm.com/book/ch5-2.html#Commit-Guidelines).
* Title that summarizes the issue.
* Description that provides a succinct executive summary of changes.

Source code changes SHOULD contain test coverage and Javadoc changes as needed.
While documentation is frequently outside the scope of a pull request, there should be some consideration for how
new features and functional changes will be documented. 


When creating a pull request, make sure that the pull references the Github issue number:

![](https://camo.githubusercontent.com/0d91dc7e679d86bd4814faae37f0316279074571/68747470733a2f2f662e636c6f75642e6769746875622e636f6d2f6173736574732f3539372f3439383937372f64383262643761382d626332362d313165322d383663652d3835613435336334643638332e706e67)

This allows the pull request to be linked to the issue. When the pull is merged, the issue will automatically be closed as well.
