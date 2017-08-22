---
layout: default
title: CAS - Contributor Guidelines
---

# Contributor Guidelines

## First

We want to start off by saying thank you for using CAS. This project is a labor of love, and we appreciate the work done by all 
who catch bugs, make performance improvements and help with documentation. Every contribution is meaningful, so thank you 
for participating. That being said, here are a few guidelines that we ask you to follow so we can successfully address your patch.

## Be Not Afraid

The goal of this guide is to, very simply put, help you learn how to catch fish. 

Shed all of your fears of making a mistake as part of your contributions to the project. Practice makes perfect and
we are all here to help you as much as we can so you can ultimately become an independent contributor to the project. It's OK
to make mistakes and it does take some time, effort and energy to get better at it. So do not be discouraged, keep at it
and if you find anything that is unclear along the way, do not hesitate to ask questions. 

## Code of Conduct

CAS is a sponsored Apereo project participating in the [Apereo Welcoming Policy](https://www.apereo.org/content/apereo-welcoming-policy).

## The Long Story

[Start from here](https://apereo.github.io/2017/07/05/cas-contribution-guide/).


## Getting Started

All CAS contributions SHOULD be made via GitHub pull requests, which requires that contributions are offered from
a fork of the [apereo CAS repository](https://github.com/apereo/CAS):

Refer to the GitHub [Fork a Repo](http://help.github.com/fork-a-repo/) page for help with forking.

The following shell commands may be used to grab the source from a forked repository:

```bash
git clone --recursive --depth=10 git@github.com:$USER/cas.git
cd cas
git remote add upstream git@github.com:apereo/cas.git
git fetch --all
```

We encourage reading [Pro Git](http://git-scm.com/book/) prior to beginning development if you are unfamiliar with Git.

## Functional Build

Before you do anything else, make sure you have a [functional build](Build-Process.html). 

## Development Process

All CAS contributions SHOULD be submitted via GitHub pull requests. The following guidelines facilitate pull requests
that have a high likelihood of acceptance:

* Work on topic branches.
* Follow the [Pro Git commit guidelines](http://git-scm.com/book/ch5-2.html#Commit-Guidelines).
* Keep patches on topic, atomic and focused on the issue.
* Try to avoid unnecessary formatting, clean-up, etc in the same patch where reasonable.

### Development Walk-Through

#### 1. Create Topic Branch

    git checkout -b CAS-123


#### 2. Edit Source and Commit

Edit source files and commit in logical chunks. We encourage numerous small commits over one large commit. Small,
focused commits facilitate review and will be more likely to be accepted. It is vital to summarize changes with
succinct commit messages. You SHOULD follow the
[Pro Git commit guidelines](http://git-scm.com/book/ch5-2.html#Commit-Guidelines) generally.

    git add --all .
    git commit -am "Update X to handle Y such that ..."
    
#### 3. Push to Forked Repository

You must push your local branch to your forked repository to facilitate a pull request.

    git push origin CAS-123


#### 4. Submit Pull Request

Submit a pull request from your topic branch onto the target branch of CAS, typically _master_. See the GitHub
[Using Pull Requests](https://help.github.com/articles/using-pull-requests) page for help.

Be prepared to sync changes with the target branch of the CAS repository since the target branch may move during review and consideration of the pull request.

If you have already identified an enhancement or a bug fix, it is STRONGLY recommended that you simply submit a pull request to address the case. There is no need for special ceremony to create separate issues.
The pull request IS the issue and it will be tracked and tagged as such. Do NOT log issues hoping for the issue to get resolved in the future by someone else. If you need additional help,
please reach out to a [commercial affiliate](https://www.apereo.org/content/commercial-affiliates) for assistance and better support.

#### Creating Pull Requests

A pull request should contain the following:

* One or more commits that follow the [Pro Git commit guidelines](http://git-scm.com/book/ch5-2.html#Commit-Guidelines).
* Title that summarizes the issue.
* Description that provides a succinct executive summary of changes.

Source code changes SHOULD contain test coverage and Javadoc changes as needed.
While documentation is frequently outside the scope of a pull request, there should be some consideration for how
new features and functional changes will be documented. 
