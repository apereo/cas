---
layout: default
title: CAS - Contributor Guidelines
---

# Contributor Guidelines


## Licensing
All developers that contribute source to CAS must complete and file a Jasig
[Individual Contributor License Agreement](https://wiki.jasig.org/x/u4WcAQ) (ICLA). This agreement provides legal
protection to both the CAS project and to individual developers while preserving copyright ownership of the author.
Any patch or pull request submitted by a developer will only be accepted if there is an ICLA on file for the
developer.


## Getting Started
All CAS contributions SHOULD be made via GitHub pull request, which requires that contributions are offered from
a fork of the Jasig CAS repository:

[https://github.com/Jasig/CAS](https://github.com/Jasig/CAS)

Refer to the GitHub [Fork a Repo](http://help.github.com/fork-a-repo/) page for help with forking.

The following shell commands may be used to grab the source from a forked repository:
{% highlight bash %}
git clone --recursive git@github.com:$USER/cas.git
cd cas
git remote add upstream git@github.com:Jasig/cas.git
git fetch --all
{% endhighlight %}

We encourage reading [Pro Git](http://git-scm.com/book/) prior to beginning development if you are unfamiliar with Git.


## Development Process
All CAS contributions SHOULD be submitted via GitHub pull requests. The following guidelines facilitate pull requests
that have a high likelihood of acceptance:

* Work on topic branches.
* Follow the [Pro Git commit guidelines](http://git-scm.com/book/ch5-2.html#Commit-Guidelines).

A [Jira](https://issues.jasig.org/browse/CAS) issue MUST be created prior to working on a contributed feature or bugfix.
It is helpful to name the topic branch the same as the Jira issue number, e.g. _CAS-123_. Additionally, the Jira issue
number MUST be the first word of a commit message; for example, _CAS-9999 Provide OAuth 3.0 protocol support_.


### Development Walk-Through

#### 1. Create Topic Branch

    git checkout -b CAS-123

We encourage naming the branch the same as the Jira issue number corresponding to the feature or bug fix.


#### 2. Edit Source and Commit
Edit source files and commit in logical chunks. We encourage numerous small commits over one large commit. Small,
focused commits facilitate review and will be more likely to be accepted. It is vital to summarize changes with
succint commit messages. You SHOULD follow the
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

<div class="alert alert-warning"><strong>Commit Titles</strong><p>The current build process will 
fail if the commit message
does not begin with a particular JIRA issue (i.e. <code>CAS-123</code>).
There are however, a number of headers reserved for special changes:

<ul>
  <li><code>CHECKSTYLE</code>: Minor formatting and convention fixes for checkstyle errors.</li>
  <li><code>JAVADOCS</code>: Minor updates to Javadocs.</li>
  <li><code>NOJIRA</code>: Minor changes to build, configuration and formatting</li>
</ul>

</p></div>

#### 3. Push to Forked Repository
You must push your local branch to your forked repository to facilitate a pull request.

    git push origin CAS-123


#### 4. Submit Pull Request
Submit a pull request from your topic branch onto the target branch of Jasig CAS, typically _master_. See the GitHub
[Using Pull Requests](https://help.github.com/articles/using-pull-requests) page for help.

Be prepared to sync changes with the target branch of the Jasig CAS repository since the target branch may move during
review and consideration of the pull request.


### Pull Requests
A pull request should contain the following:

* One or more commits that follow the [Pro Git commit guidelines](http://git-scm.com/book/ch5-2.html#Commit-Guidelines).
* Title that includes the Jira issue number.
* Description that provides a succinct executive summary of changes.

Source code changes SHOULD contain test coverage and Javadoc changes as needed.
While documentation is frequently outside the scope of a pull request, there should be some consideration for how
new features and functional changes will be documented. It may be helpful to create a separate Jira issue to track
documentation and link it to the primary Jira issue as a subtask or dependency.

The Jira issue should be updated with comments to cross reference the pull and subsequent merge:

* Link to pull request upon creation.
* Link to GitHub commit where pull is merged upon acceptance.

Typically the Jira issue is closed after the pull request is accepted.
