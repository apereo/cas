---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - GCP Secret Manager

CAS is also able to use [Google Cloud Secret Manager](https://cloud.google.com/secret-manager) to locate properties and settings.
This functionality is delivered to CAS via [Spring Cloud GCP](https://github.com/GoogleCloudPlatform/spring-cloud-gcp).

Support is provided via the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-gcp-secretsmanager" %}

<div class="alert alert-info mt-3">:information_source: <strong>Usage</strong><p>The configuration modules provided here may also be used 
verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is 
primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay 
directly to fetch settings from a source.</p></div>

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.gcp.secretmanager" %}

## Secret Manager Config Data Resource

The functionality presented here enables you to use Secret Manager as an external config data 
resource which means it allows you to specify and load secrets from Google Cloud Secret Manager as properties into the 
application context using Spring Boot’s Config Data API.

The Secret Manager config data resource uses the following syntax to specify secrets:

```properties
# 1. Long form - specify the project ID, secret ID, and version
sm://projects/<project-id>/secrets/<secret-id>/versions/<version-id>}                           

# 2.  Long form - specify project ID, secret ID, and use latest version
sm://projects/<project-id>/secrets/<secret-id>

# 3. Short form - specify project ID, secret ID, and version
sm://<project-id>/<secret-id>/<version-id>

# 4. Short form - default project; specify secret + version
#
# The project is inferred from the spring.cloud.gcp.secretmanager.project-id setting
# in your bootstrap.properties (see Configuration) or from application-default credentials if
# this is not set.
sm://<secret-id>/<version>

# 5. Shortest form - specify secret ID, use default project and latest version.
sm://<secret-id>
```

You can use this syntax in your CAS configuration:

```properties
# This may be optional
# spring.config.import=sm://

# Example of the project-secret long-form syntax.
cas.some.property=${sm://projects/<project-id>/secrets/cas_some_property}
```
