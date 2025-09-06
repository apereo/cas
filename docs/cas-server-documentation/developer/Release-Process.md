---
layout: default
title: CAS - Release Process
category: Developer
---

{% include variables.html %}

# CAS Release Process

This page documents the steps that a release engineer should take for cutting a CAS server release.

There are two ways to run the release process:

- **Locally**: You can run the release process locally on your machine, which requires you to have a few things set up in advance noted below.
- **GitHub Actions**: You can run the release process via GitHub Actions, which is the preferred way to run the release process. This does not require you to have much set up in advance, and you can simply run the release process by dispatching a workflow run on GitHub after having updated versions, etc.
                                                                                                                                                   
As you go through this document, you will find that the steps are mostly the same for both approaches, with some minor differences.

## Maven Central Setup

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>
This step is only relevant if you wish to run the release process locally.</p></div>

You will need to sign up for an [account](https://central.sonatype.com/) and must ask 
to be authorized to publish releases to the `org.apereo` namespace. You may be asked to have one of the
current project members *vouch* for you.

## GPG Setup

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>
This step is only relevant if you wish to run the release process locally.</p></div>

You will need to generate your own PGP signatures to 
sign the release artifacts prior to uploading them to a central repository. In order to create OpenPGP signatures, you will 
need to generate a key pair. You need to provide the build with your key information, which means three things:

- The public key ID (The last 8 symbols of the keyId. You can use `gpg -K` to get it).
- The absolute path to the secret key ring file containing your private key. Since gpg 2.1, you need to export the keys with command:

```bash
gpg --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg
```

- The passphrase used to protect your private key.

The above settings will need to be placed into the `~/.gradle/gradle.properties` file:

```properties
signing.keyId=7A24P9QB
signing.password=P@$$w0rd
signing.secretKeyRingFile=/Users/example/.gnupg/secring.gpg
```

Additional notes on how artifacts are signed using the Gradle 
signing plugin are [available here](https://docs.gradle.org/current/userguide/signing_plugin.html)

## Environment Setup

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>
This step is only relevant if you wish to run the release process locally.</p></div>

- Load your SSH key and ensure this SSH key is also referenced in GitHub.
- Adjust `$GRADLE_OPTS` to initialize the JVM heap size, if necessary.
- Load your `~/.gradle/gradle.properties` file with the following *as an example*:

```properties
org.gradle.daemon=false
org.gradle.parallel=false
```

- Checkout the CAS project: `git clone git@github.com:apereo/cas.git cas-server`
- Make sure you have the [latest version of JDK 21](https://openjdk.java.net/projects/jdk/21/) installed via `java -version`. 

## Preparing the Release

Apply the following steps to prepare the release environment. There 
are a few variations to take into account depending on whether
a new release branch should be created. 

### Create Branch

```bash
# Replace $BRANCH with CAS version (i.e. 6.5.x)
git checkout -b $BRANCH
```

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>You should do this only for major or minor 
releases (i.e. <code>4.2.x</code>, <code>5.0.x</code>).
If there already exists a remote tracking branch for the version you are about to release, you should <code>git checkout</code> that branch, 
skip this step and move on to next section to build and release.</p></div>

### GitHub Actions

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>You should do this only for major or minor 
releases, when new branches are created.</p></div>
 
Change GitHub Actions workflows to trigger and *only* build the newly-created release branch:

* `analysis.yml`
* `validation.yml`
* `publish.yml`
* `publish-docs.yml`
* `functional-tests.yml`
* `tests.yml`
      
```yaml
on:
  workflow_dispatch:
  push:
    branches:
      - $NEW_BRANCH
      - '!**.**.**'
      - '!heroku-*'
  pull_request:
    types: [ labeled ]
    branches: [ $NEW_BRANCH, pr-* ]
```

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>Make sure nothing else in these workflows
points to the <code>master</code> branch</p></div>

Then disable the following workflows:

- `build.yml`
- `dependencies.yml`
- `native-tests.yml`

These can be disabled in the YAML configuration via:

```yaml
on:
  workflow_dispatch:  
  push:
    branches-ignore:
      - '**'
```

Do not forget to commit all changes and push changes upstream, creating a new remote branch to track the release.

## Performing the Release 

The CAS release can be performed using the following options.

### GitHub Actions (Preferred)

- Once you're on the right branch in the project's `gradle.properties`, change the project version to the release version and remove the `-SNAPSHOT`. (i.e. `6.0.0-RC1`).
- Commit and push the changes to the remote branch. Make sure the commit message includes the message `[skip ci]` to skip the CI build to save time.
- Go to [GitHub Actions](https://github.com/apereo/cas/actions) and dispatch the `Release` workflow run on the right branch. This will trigger the release process.

The workflow will automatically build the project, sign the artifacts, stage them to central repository, 
and create a tag for the released version. It will also create a GitHub release for the version, with
appropriate release notes and artifacts attached to the release, which you will need to review and publish later to finalize the release.

Once the release is staged and has passed validation, it will be published automatically.

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>
If the deployment is stuck in a <code>PUBLISHING</code> state for a long time (i.e. more than 1 hour), contact Central support. 
In case ingesting one of the CAS artifacts fails, the release process fails to notify the Central publisher that the ingesting was completed
and the deployment is left in a <code>PUBLISHING</code> state.
</p></div>

### Locally

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>
You should only do this if you <strong>REALLY</strong> know what you're doing. This step is reserved
best for troubleshooting and diagnostics.</p></div>

- In the project's `gradle.properties`, change the project version to the release version and remove the `-SNAPSHOT`. (i.e. `6.0.0-RC1`). Commit the change.
- You need to specify the credentials beforehand as environment variables:

```json
# Credentials here are generated at https://central.sonatype.com/account
# You will receive a userid and a user token.
export REPOSITORY_USER="..."
export REPOSITORY_PWD="..."
```

- Build and release the project using the following command:

```bash
./ci/release.sh
```

## Finalizing the Release

You should now switch back to the main development branch (i.e. `master`) and follow these steps:

- In the project's `gradle.properties`, change the project version to the *next* development version (i.e. `5.0.0-SNAPSHOT`). 
- Push your changes to the upstream repository. 

## Housekeeping

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>When updating the release description, try to be keep 
consistent and follow the same layout as previous releases.</p></div>

Remember to mark the release tag as pre-release if not done already, when releasing RC versions of the project on GitHub.
If the GitHub release is already created, review the release description, edit details if needed and publish it.

## Update CAS Initializr

Make sure to update the CAS Initializr to allow for generation of projects
based on the newly-released version.

## Update Documentation

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>
You should do this only for major or minor releases, when new branches are created.</p></div>

- Configure docs to point `current` to the latest available version [here](https://github.com/apereo/cas/blob/gh-pages/current/index.html).
- Modify [the configuration file](https://github.com/apereo/cas/blob/master/docs/cas-server-documentation/_config.yml) to exclude relevant branches and directories from the build. 
- Configure docs to include the new release in the list of [available versions](https://github.com/apereo/cas/blob/master/docs/cas-server-documentation/_layouts/default.html).
- Update the project's [`README.md` page](https://github.com/apereo/cas/blob/master/README.md) to list the new version, if necessary.
- Update [the build process](Build-Process.html) to include any needed information on how to build the new release.
- Update [Algolia](https://crawler.algolia.com/) for the new documentation version to index the new space for search requests.
- Update the release notes overview and remove all previous entries.

## Update Maintenance Policy

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>You should do this only for major or minor releases, when new branches are created.</p></div>

Update the [Maintenance Policy](https://github.com/apereo/cas/edit/gh-pages/developer/Maintenance-Policy.md/) to note 
the release schedule and EOL timeline.
