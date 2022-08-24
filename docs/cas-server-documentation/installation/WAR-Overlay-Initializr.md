---
layout: default
title: CAS - WAR Overlay Initializr
category: Installation
---
{% include variables.html %}

# WAR Overlay Initializr

[Apereo CAS Initializr][initializr] is a components in the Apereo CAS ecosystem that allows
you as the deployer to generate CAS WAR Overlay projects on the fly with just what you need to start quickly.

To get started with a CAS deployment, adopters typically
start with [a plain Gradle-based overlay project](WAR-Overlay-Installation.html)
and use that as a baseline for future modifications. While this has been the traditional and recommended
approach for many years, it can also be rather challenging for a relatively-novice user new to the
ecosystem to download, modify and prepare an overlay project to include all required
customizations. Given the overlay project’s static nature, it can also be challenging for
project owners and maintainers to keep it up-to-date or offer additional enhancements
and automation without affecting the baseline template.

To address such scenarios, the [CAS WAR Overlay Initializr][initializr] offers a fast way to pull
in all the dependencies and modules needed for a CAS deployment and
provides friendly and programmatic curl-friendly APIs to generate
an overlay structure and required build files.

The underlying framework that handles the project generation
task is based on [Spring Initializr](https://github.com/spring-io/initializr).

## Overview

The CAS Initializr can dynamically generate a starting project based on
requested modules and dependencies needed for a deployment. This behavior
can be tailored to the user’s experience based on that input and the
conditions that follow to generate additional references, files, starting
templates, and more in the same project to make the deployment process more comfortable.

Managing and maintaining separate overlay projects and keeping them in sync with various CAS versions can be a costly maintenance task.
CAS Initializr allows the project developers to automate the maintenance task, keep everything in the same repository
for faster and more accurate upgrades.

CAS Initializr is used internally by the CAS project itself in a very *Eat What You Kill* type of way to dynamically generate
overlay projects. These generated projects are used as CAS base Docker images published to Docker Hub, and as a baseline for
browser/UI tests run by the CAS CI for each relevant feature. CAS Initializr uses itself to test itself!

To properly initialize and generate a CAS overlay, you may want to make
sure you have `curl`, `http` and/or [jq](https://stedolan.github.io/jq/) installed.

## Project Types

The [CAS Initializr][initializr] can be invoked using curl to generate different types of overlay projects.
The project selection is indicated using a `type` parameter. The following types are supported:

| Type                           | Description                                                                                                                 |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `cas-overlay`                  | Default; generates a [CAS WAR overlay](../installation/WAR-Overlay-Installation.html).                                      |
| `cas-bootadmin-server-overlay` | Generates a WAR Overlay for the [Spring Boot Admin Server](../monitoring/Configuring-Monitoring-Administration.html).       |
| `cas-config-server-overlay`    | Generates a WAR Overlay for the [Spring Cloud Configuration Server](../configuration/Configuration-Server-Management.html). |
| `cas-discovery-server-overlay` | Generates a WAR Overlay for the [Service Discovery Server](../installation/Service-Discovery-Guide-Eureka.html).            |
| `cas-management-overlay`       | Generates a WAR Overlay for the [CAS Management Web Application](../services/Installing-ServicesMgmt-Webapp.html).          |

## Project Versions

The [CAS Initializr][initializr] can be instructed to prepare an overlay project for a specific version.
The following request parameters control the version selection:

| Type          | Description                                                     |
|---------------|-----------------------------------------------------------------|
| `casVersion`  | Specify the CAS version for the overlay.                        |
| `bootVersion` | **(Optional)** Specify the Spring Boot version for the overlay. |

Note that mixing and matching different versions generally will lead to unstable projects builds. Do so at your own risk! The specification
of version numbers in the project generation request is unnecessary, and [CAS Initializr][initializr] will always
use trusted defaults to handle the generation task.

To review the list of supported versions for the CAS Initializr on [Heroku][initializr], you may use:

```bash
curl <cas-initializr-url>/actuator/supportedVersions | jq
```

## Source Code

[CAS Initializr][initializr] is available at:

| Source Repository                          | Location           |
|--------------------------------------------|--------------------|
| `https://github.com/apereo/cas-initializr` | [Link][initializr] |

## Project Generation

The [CAS Initializr][initializr] can be invoked using curl to generate an overlay project. To access
the CAS Initializr, the following strategies can be used.

### Heroku

The CAS projects provides a public **paid-for-by-Apereo-members** instance of the CAS Initializr on [Heroku][initializr], paid for by the
Apereo CAS support subscribers. To get started with this instance, a simple way might be to include the following function in your bash profile:

```bash
function getcas() {
  url="https://casinit.herokuapp.com/starter.tgz"
  projectType="cas-overlay"
  dependencies=""
  directory="overlay"
  for arg in $@; do
    case "$arg" in
    --url|-u)
      url=$2
      shift 1
      ;;
    --type|-t)
      projectType=$2
      shift 1
      ;;
    --directory|--dir|-d)
      directory=$2
      shift 1
      ;;
    --casVersion|--cas)
      casVersion="-d casVersion=$2"
      shift 1
      ;;
    --bootVersion|--springBootVersion|--boot)
      bootVersion="-d bootVersion=$2"
      shift 1
      ;;
    --modules|--dependencies|--extensions|-m)
      dependencies="-d dependencies=$2"
      shift 1
      ;;
    *)
      shift
      ;;
    esac
  done
  rm -Rf ./${directory}
  echo -e "Generating project ${projectType} with dependencies ${dependencies}..."
  cmd="curl ${url} -d type=${projectType} -d baseDir=${directory} ${dependencies} ${casVersion} ${bootVersion} | tar -xzvf -"
  echo -e "${cmd}"
  eval "${cmd}"
  ls
}
```

<div class="alert alert-info"><strong>Note</strong><p>
If you prefer, you could invoke the <code>/starter.zip</code> endpoint to get back a ZIP file instead.
</p></div>

This allows you to generate a CAS overlay project in the `overlay` directory using:

```bash
getcas --modules duo,oidc
```

…which generates a CAS overlay project prepared with multifactor authentication
by [Duo Security](../mfa/DuoSecurity-Authentication.html) and
support for [OpenID Connect](../authentication/OAuth-Authentication.html).

<div class="alert alert-info"><strong>Note</strong>
<p>To help keep the deployment costs down, the Heroku instance has turned on support for
rate-limiting requests. Be aware that frequent requests may be throttled for access.</p></div>

### Docker

In case the Initializr is not available on Heroku, you can also run your own Initializr instance via Docker:

```bash
docker run --rm -p 8080:8080 apereo/cas-initializr:${tag}
```

The CAS Initializr should become available at `http://localhost:8080` and will respond to API
requests using curl. Published images and tags of the CAS Initializr [can be found here](https://hub.docker.com/r/apereo/cas-initializr/tags).

## CAS Modules

CAS project modules and dependencies that can be requested must be specified by
their identifier. To see a full list of all dependencies supported and
available by this service, you can invoke the following command:

```bash
curl <cas-initializr-url>/dependencies
```

Typically, dependency identifiers match CAS server dependency/module artifact names without
the `cas-server-` prefix. Furthermore, certain dependencies are assigned aliases as
shortcuts to simplify requests. To see the full list of dependencies and their aliases, you may use:

```bash
curl <cas-initializr-url>/actuator/info | jq
```

Furthermore, CAS Initializr publishes metadata about its capabilities, that is the
available options for all request parameters (dependencies, type, etc.) A client to the
service uses that information to initialize the select options and the tree of available dependencies.

You can grab the metadata on the root endpoint with the appropriate `Accept` header:

```bash
curl -H 'Accept: application/json' <cas-initializr-url> | jq
```

Or using HTTPie:

```bash
http <cas-initializr-url> Accept:application/json | jq
```

### Dependency Management

CAS Initializr is set to use the [CAS BOM](BOM-Dependency-Management.html) for dependency management.

[initializr]: https://casinit.herokuapp.com/
