---
layout: default
title: CAS - Release Notes
category: Planning
---

{% include variables.html %}

# 8.0.0-RC3 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks,
statistics or completion of features. To gain confidence in a particular
release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you
to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support,
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner.

## Get Involved

- Start your CAS deployment today. Try out features and [share feedback](/cas/Mailing-Lists.html).
- Better yet, [contribute patches](/cas/developer/Contributor-Guidelines.html).
- Suggest and apply documentation improvements.

## Resources

- [Release Schedule](https://github.com/apereo/cas/milestones)
- [Release Policy](/cas/developer/Release-Policy.html)

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `25`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### OpenRewrite Recipes

CAS continues to produce and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations
in place from one version to the next. [See this guide](../installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html). We continue to polish native runtime hints.
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `539` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

Furthermore the test scenario construction logic is given the ability to limit its run to a pre-specified
time window, which is specially useful when when the test requires external resources and APIs that may
not be available at all times.

### Gradle 9.4

CAS is now built with Gradle `9.4` and the build process has been updated to use the latest Gradle
features and capabilities. This also prepares future CAS versions to build and run against JDK `26`.

### Spring Boot 4.1

CAS is now built with Spring Boot `4.1.x`. This is a major platform upgrade that affects almost all aspects of the codebase
including many of the third-party core libraries used by CAS as well as some CAS functionality.

### JSpecify & NullAway

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.
   
### SPIFFE Support

CAS now supports [SPIFFE](https://spiffe.io/), used for client mTLS authentication in OAuth and OpenID Connect flows.
It’s a method where an OAuth client authenticates to CAS using its SPIFFE-issued identity, 
instead of a static secret or manually managed certificate. 
[CAS gains the ability](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html) to extract and identify the SPIFFE ID 
in the certtificate and map it to the client application definition. 
 
### SAML2 Service Provider Metadata Management

CAS may be configured to use and locate SAML2 service provider metadata in external resources, such as MongoDb.
The existing integration for persistence layer is now improved to fully support common metadata management tasks
such as create, read, update and delete (CRUD) operations. This allows one to manage SAML2 service provider metadata
without having to manually interact with the underlying database.

The storage options that can take advantage of this include:

- [MongoDb](../installation/Configuring-SAML2-DynamicMetadata-MongoDb.html)
- [SQL Databases](../installation/Configuring-SAML2-DynamicMetadata-JPA.html)
- [Amazon S3](../installation/Configuring-SAML2-DynamicMetadata-AmazonS3.html)
- [Amazon DynamoDb](../installation/Configuring-SAML2-DynamicMetadata-DynamoDb.html)
- [Git](../installation/Configuring-SAML2-DynamicMetadata-Git.html)
- [Redis](../installation/Configuring-SAML2-DynamicMetadata-Redis.html)
- [REST](../installation/Configuring-SAML2-DynamicMetadata-REST.html)

There is also a new actuator endpoint, `samlIdPRegisteredServiceMetadata`, that allows one to interact with
the SAML2 service provider metadata management API and perform CRUD operations on the metadata of registered services
assuming the underlying storage is one that is noted above.

The [Palantir Admin Console](../installation/Admin-Dashboard.html) is also updated to support interacting 
with this actuator endpoint.
 
### Apache Tomcat Connectors

When running CAS with an embedded Apache Tomcat, all connectors are now initially put into a *paused state* 
until the CAS server is fully able and ready to accept requests at which point these connectors are instructed to resume operations. 
This allows the server to perform all necessary startup and (lazy) initialization tasks before accepting any 
incoming traffic and potentially running into a deadlocked state.

<div class="alert alert-info">:information_source: <strong>Note</strong>
<p>Again, this capability only applies to deployments running with an embedded Apache Tomcat.
It has no effect on other container types such as Jetty, or those deployments that run
inside an external self-managed Apache Tomcat environment.</p></div>

### Etcd Configuration Source

CAS is now able to use [etcd](../configuration/Configuration-Server-Management-SpringCloud-Etcd.html) 
as a configuration source to locate properties and settings.

## Other Stuff

- SAML2 logout requests for SOAP bindings correct the `Content-Type` header and formatted body.
- JSON web keys that use the `EC` algorithm can now be used for token validation operations.
