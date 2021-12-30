---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud Default

The Spring Cloud Configuration Server is able to handle `git` or `svn` based repositories that host CAS configuration.
Such repositories can either be local to the deployment, or they could be on the cloud in form of GitHub/BitBucket. Access to
cloud-based repositories can either be in form of a username/password, or via SSH so as long the appropriate keys are configured in the
CAS deployment environment which is really no different than how one would normally access a git repository via SSH.

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.config.server.git,spring.cloud.config.server.svn" %}

<div class="alert alert-info mt-3"><strong>Usage</strong><p>The configuration modules provide here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>


Needless to say, the repositories could use both YAML and properties syntax to host configuration files.
The default profile is activated using `spring.profiles.active=default`.

<div class="alert alert-info"><strong>Keep What You Need!</strong><p>Again, in all of the above strategies,
an adopter is encouraged to only keep and maintain properties needed for their particular deployment. It is
UNNECESSARY to grab a copy of all CAS settings and move them to an external location. Settings that are
defined by the external configuration location or repository are able to override what is provided by CAS
as a default.</p></div>

Load settings from external properties/yaml configuration files.

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.config.server.default"
thirdPartyExactMatch="spring.profiles.active"
%}

## Git Repository

Allow the CAS Spring Cloud configuration server to load settings from an internal/external Git repository.
This then allows CAS to become a client of the configuration server, consuming settings over HTTP where needed.

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.config.server.git"
thirdPartyExactMatch="spring.profiles.active"
%}

The above configuration also applies to online git-based repositories such as Github, BitBucket, etc.

