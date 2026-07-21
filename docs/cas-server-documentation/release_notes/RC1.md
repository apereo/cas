---
layout: default
title: CAS - Release Notes
category: Planning
palantir_images:
  - src: img_1.png
    alt: Palantir scripting view
    title: Palantir scripting view
  - src: img_2.png
    alt: Palantir applications view
    title: Palantir applications view
  - src: img_3.png
    alt: Palantir Heimdall authorization view
    title: Palantir Heimdall authorization view
  - src: img_4.png
    alt: Palantir Heimdall authorization view
    title: Palantir Heimdall authorization view
  - src: img_5.png
    alt: Palantir attribute repositories view
    title: Palantir attribute repositories view
  - src: img.png
    alt: Palantir Groovy script view
    title: Palantir Groovy script view
  - src: img_6.png
    alt: Palantir attribute repositories view
    title: Palantir attribute repositories view
  - src: img_7.png
    alt: Palantir Groovy script view
    title: Palantir Groovy script view
  - src: img_8.png
    alt: Palantir authorization simulation view
    title: Palantir authorization simulation view
  - src: img_9.png
    alt: Palantir cluster topology view
    title: Palantir cluster topology view
  - src: img_10.png
    alt: Palantir cluster topology view
    title: Palantir cluster topology view
  - src: img_11.png
    alt: Palantir passwordless authentication view
    title: Palantir passwordless authentication view
---

{% include variables.html %}

# 8.1.0-RC1 Release Notes

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
and scenarios. At the moment, total number of jobs stands at approximately `556` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Gradle 9.7

CAS is now built with Gradle 9.7 and the build process has been updated to use the 
latest Gradle features and capabilities.
 
### Spring Boot 4.2

CAS is now built with Spring Boot `4.2.x`. This is a minor platform upgrade that 
affects almost all aspects of the codebase including many of the third-party 
core libraries used by CAS as well as some CAS functionality.

### JSpecify & NullAway

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### OAuth & OpenID Connect Client Secrets

OAuth and OIDC client applications may now define [multiple client secrets](../authentication/OAuth-ClientSecret-Management.html), 
allowing deployments to support secret expiration and smoother secret rotation. Existing 
single-secret configurations remain compatible, while new configurations can 
include additional secrets with expiration metadata so clients can transition 
to new secrets without immediate disruption. Client secret rotation may be carried out using
a dedicated `oauthClientSecrets` actuator endpoint.

### Attribute Definition Dependencies

Attribute definitions may now [declare dependencies](../integration/Attribute-Definitions.html) on other attribute definitions. 
When an attribute is resolved, its declared dependencies are resolved first and their results are made available 
during resolution, allowing definitions to build on values produced by other definitions in a predictable, reusable way.
  
### Palantir Admin Dashboard

Inlined table buttons in [Palantir Admin Dashboard](../installation/Admin-Dashboard.html) are 
replaced with proper context menus triggered by right clicks. The configuration tab is also extended 
to display cached scripted resources with the ability to either remove or recompute the cache entry.
     
Furthermore, [Heimdall authorization policies](../authorization/Heimdall-Authorization-Overview.html)
can now be created, edited and removed from the [Palantir Admin Dashboard](../installation/Admin-Dashboard.html).
There is also dedicated simulation support to experiment with authorization requests.

Configuration for almost all attribute repositories can also be viewed in better detail. Fields that do support
[inline Groovy scripts](../integration/Apache-Groovy-Scripting.html) are also allowed to better receive their 
value from a dedicated editor.

{% include imagegallery.html gallery_id="palantir-dashboard" images=page.palantir_images %}

### Palantir User vs. Admin Roles

The [Palantir Admin Dashboard](../installation/Admin-Dashboard.html) now supports basic user roles and permissions. 
By default, all authenticated users are assigned a `ROLE_USER` role/authority. To access critical functionality as an admin, 
you will need to resolve and release a `role` attribute to Palantir with a value of `ADMIN` or `ROLE_ADMIN` if you are
accessing Palantir via external CAS authentication, or your configuration needs to assign the authenticated user role
via `spring.security.user.roles=ADMIN`.
  
At this moment, all Palantir functionality is disabled and hidden for non-admin users, except 
for the ability to manage the list of registered applications.

### Cluster Topology 

A new `clusterTopology` actuator endpoint is available to report on the current cluster topology 
and the status of each node in the cluster, particularly relevant when CAS is running in high-availability mode.
Cluster topology support is available for the following features:

- [MongoDb Ticket Registry](../ticketing/MongoDb-Ticket-Registry.html)
- [Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html)
- [Hazelcast Ticket Registry](../ticketing/Hazelcast-Ticket-Registry.html)
- [Apache Ignite Ticket Registry](../ticketing/Ignite-Ticket-Registry.html)
- [Apache Kafka Ticket Registry](../ticketing/Kafka-Ticket-Registry.html)
- [Apache Pulsar Ticket Registry](../ticketing/Pulsar-Ticket-Registry.html)
- [Apache Geode Ticket Registry](../ticketing/Geode-Ticket-Registry.html)
- [AMQP Ticket Registry](../ticketing/Messaging-AMQP-Ticket-Registry.html)

This capability is also supported and available for the [Palantir Admin Dashboard](../installation/Admin-Dashboard.html). 
          
### Passwordless Authentication
                   
A dedicated actuator endpoint, `passwordless`, is available to allows one to query a username
and retrieve the associated [passwordless account](../authentication/Passwordless-Authentication-Account-Storage.html) information. 
This capability is also supported and available for the [Palantir Admin Dashboard](../installation/Admin-Dashboard.html).

## Other Stuff
  
- Multifactor authentication may also be activated using [SAML2 metadata entity attributes](../mfa/Configuring-Multifactor-Authentication-Triggers-EntityId.html).
- Releasing attributes via [pattern matching](../integration/Attribute-Release-Policy-PatternMatching.html) accepts Groovy transformation rules.
- A number of date-formatting operations have switched their base timezone from system default to `UTC`.
- A large number of dependencies and libraries have been updated to their latest versions.
- Custom ID token claims can also be constructed using [Apache Groovy](../authentication/OIDC-Authentication-Claims-Custom.html).
- [RediSearch](../ticketing/Redis-Ticket-Registry-RediSearch.html) functionality now supports Redis clustering.
