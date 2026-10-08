---
layout: default
title: CAS - 8.1.0-RC1 Release Notes
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
  - src: img_12.png
    alt: Palantir attribute repositories view
    title: Palantir attribute repositories view
  - src: img_13.png
    alt: Palantir attribute repositories view
    title: Palantir attribute repositories view
release:
  line: 8.1.0 development
  summary: >-
    The first release candidate of the 8.1.0 line turns Palantir into a place to manage authorization policies,
    attribute repositories and cluster topology, and lets OAuth and OpenID Connect applications rotate multiple client secrets.
  facts:
    - label: Requires
      value: JDK 25
      url: ../planning/Installation-Requirements.html
    - label: Built on
      value: Spring Boot 4.2
    - label: Built with
      value: Gradle 9.7
    - value: "556"
      suffix: browser test scenarios
      url: ../../developer/Test-Process.html
  upgrade:
    - type: action
      title: Palantir admin functions
      text: now require a released `role` attribute of `ADMIN` or `ROLE_ADMIN`; other users only manage registered applications.
      section: Palantir User vs. Admin Roles
      area: operations
    - type: changed
      title: Transient session tickets
      text: now live for `5` minutes by default instead of `15`.
      section: Tickets & Sessions
      area: tickets
    - type: changed
      title: Date formatting
      text: in a number of operations now uses `UTC` instead of the system default time zone.
      section: General
    - type: changed
      title: CAS REST APIs
      text: answer unauthorized application requests with `403` instead of a `500` error.
      section: CAS & Other Protocols
      area: protocols
    - type: removed
      title: LettuceMod
      text: is removed; Lettuce itself now provides its functionality.
      section: Tickets & Sessions
      area: tickets
  spotlight:
    section: Palantir Admin Dashboard
    kicker: Palantir admin dashboard
    title: Manage Authorization from the Dashboard
    images: palantir_images
    points:
      - Create, edit and remove Heimdall authorization policies, and simulate authorization requests.
      - Right-click context menus replace inline table buttons.
      - View attribute repositories in detail and register LDAP, JDBC and stub repositories.
      - Inspect, remove or recompute cached scripted resources, and edit inline Groovy in a dedicated editor.
  highlights:
    - section: OAuth & OpenID Connect Client Secrets
      title: Multiple Client Secrets
      summary: Applications can hold several expiring client secrets for smooth rotation, managed through the `oauthClientSecrets` endpoint.
    - section: Cluster Topology
      summary: A `clusterTopology` actuator endpoint reports every node and its status across eight ticket registries.
    - section: Attribute Definition Dependencies
      summary: Attribute definitions can depend on other definitions, which are resolved first and available during resolution.
    - section: OpenID Connect Verifiable Credentials
      summary: Credential issuance supports the authorization code flow, more formats and initial OpenID4VP support.
    - section: OpenID Connect Federation
      summary: Federation support is roughly finalized, with remaining edge cases tracked for the next releases.
    - section: Acceptable Usage Policy & Multitenancy
      summary: Acceptable usage policies work per tenant, starting with MongoDb storage.
---

{% include variables.html %}

{% include release-digest.html %}

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### OpenRewrite Recipes
{: data-area="operations"}

CAS continues to produce and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations
in place from one version to the next. [See this guide](../installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images
{: data-area="operations"}

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html). We continue to polish native runtime hints.
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy
{: data-area="project"}

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `556` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Gradle 9.7
{: .changed data-area="project"}

CAS is now built with Gradle 9.7 and the build process has been updated to use the 
latest Gradle features and capabilities.
 
### Spring Boot 4.2
{: .changed data-area="project"}

CAS is now built on top of Spring Boot `4.2.x`. This is an in-progress ongoing minor platform upgrade that 
affects almost all aspects of the codebase including many of the third-party core libraries used by CAS 
as well as some CAS functionality.

Please refer to the [Spring Boot Wiki](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.2-Release-Notes) 
for more information on the changes and updates in this release. The biggest change to CAS would be support for AMQP 1.0.

### JSpecify & NullAway
{: .new data-area="project"}

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### OAuth & OpenID Connect Client Secrets
{: .new data-area="oidc"}

OAuth and OIDC client applications may now define [multiple client secrets](../authentication/OAuth-ClientSecret-Management.html), 
allowing deployments to support secret expiration and smoother secret rotation. Existing 
single-secret configurations remain compatible, while new configurations can 
include additional secrets with expiration metadata so clients can transition 
to new secrets without immediate disruption. Client secret rotation may be carried out using
a dedicated `oauthClientSecrets` actuator endpoint.

### Attribute Definition Dependencies
{: .new data-area="attributes"}

Attribute definitions may now [declare dependencies](../integration/Attribute-Definitions.html) on other attribute definitions. 
When an attribute is resolved, its declared dependencies are resolved first and their results are made available 
during resolution, allowing definitions to build on values produced by other definitions in a predictable, reusable way.
  
### Palantir Admin Dashboard
{: .new data-area="operations"}

Inlined table buttons in [Palantir Admin Dashboard](../installation/Admin-Dashboard.html) are 
replaced with proper context menus triggered by right clicks. The configuration tab is also extended 
to display cached scripted resources with the ability to either remove or recompute the cache entry.
     
Furthermore, [Heimdall authorization policies](../authorization/Heimdall-Authorization-Policies.html)
can now be created, edited and removed from the [Palantir Admin Dashboard](../installation/Admin-Dashboard.html).
There is also dedicated simulation support to experiment with authorization requests.

Configuration for almost all attribute repositories can also be viewed in better detail. The dashboard
is also able to register new attribute repositories for LDAP, JDBC and Stub repositories.
                         
Finally, fields that do support [inline Groovy scripts](../integration/Apache-Groovy-Scripting.html) are also allowed to better receive their value from a dedicated editor.


### Palantir User vs. Admin Roles
{: .action data-area="operations"}

The [Palantir Admin Dashboard](../installation/Admin-Dashboard.html) now supports basic user roles and permissions. 
By default, all authenticated users are assigned a `ROLE_USER` role/authority. To access critical functionality as an admin, 
you will need to resolve and release a `role` attribute to Palantir with a value of `ADMIN` or `ROLE_ADMIN` if you are
accessing Palantir via external CAS authentication, or your configuration needs to assign the authenticated user role
via `spring.security.user.roles=ADMIN`{: .cas-setting}.
  
At this moment, all Palantir functionality is disabled and hidden for non-admin users, except 
for the ability to manage the list of registered applications.

### Cluster Topology 
{: .new data-area="operations"}

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
{: .new data-area="passwordless"}
                   
A dedicated actuator endpoint, `passwordless`, is available to allows one to query a username
and retrieve the associated [passwordless account](../authentication/Passwordless-Authentication-Account-Storage.html) information. 
This capability is also supported and available for the [Palantir Admin Dashboard](../installation/Admin-Dashboard.html).
 
### OpenID Connect Verifiable Credentials
{: .new data-area="oidc"}

[OpenID Connect with Verifiable Credentials](../authentication/OIDC-Authentication-Verifiable-Credentials.html) now 
supports the authorization code flow. There are also significant changes in place to support more formats
with additional bug fixes and enhancements, and an improved test suite to verify credential issuance.
There is also initial support for OpenID Connect with Verifiable Presentations (OpenID4VP).
    
### OpenID Connect Federation
{: .changed data-area="oidc"}

Work on [OpenID Connect Federation](../authentication/OIDC-Authentication-Federation.html) is now roughly finalized. A number of test scenarios and minor edge cases
are still being worked on and should be resolved in the next few releases.

### Acceptable Usage Policy & Multitenancy
{: .new data-area="ui"}

[Acceptable Usage Policy (AUP) support](../webflow/Webflow-Customization-AUP.html) has been extended to 
support multitenancy. A number of storage mechanisms are now available to support multitenancy, specifically 
[MongoDb](../webflow/Webflow-Customization-AUP-MongoDb.html) and more will be added in the future.

## Other Stuff
  
- {: .new} Multifactor authentication may also be activated using [SAML2 metadata entity attributes](../mfa/Configuring-Multifactor-Authentication-Triggers-EntityId.html).
- {: .new} Releasing attributes via [pattern matching](../integration/Attribute-Release-Policy-PatternMatching.html) accepts Groovy transformation rules.
- {: .changed} A number of date-formatting operations have switched their base timezone from system default to `UTC`.
- {: .changed data-area="project"} A large number of dependencies and libraries have been updated to their latest versions.
- {: .new} Custom ID token claims can also be constructed using [Apache Groovy](../authentication/OIDC-Authentication-Claims-Custom.html).
- {: .new} [RediSearch](../ticketing/Redis-Ticket-Registry-RediSearch.html) functionality now supports Redis clustering.
- {: .changed data-area="tickets"} The maximum lifetime of a transient session ticket (i.e. `TST`) is by default reduced from `15` minutes to `5` minutes.
- {: .changed} Groovy integration tests have now switched to use Groovy `5.1.x`.
- {: .changed} Redis integration tests have now switched to use Redis `8.10.x`.
- {: .new} Attributes requested for [Consent](../integration/Attribute-Release-Consent.html) may now be localized using language bundles and a prefixed language key that is `screen.consent.attributes.attribute.[attribute-name]`.
- {: .changed} When removing cookies, particularly during logout, the existing cookie value is no longer echoed back for remove operations.
- {: .changed} [CAS REST APIs](../protocol/REST-Protocol.html) now return a `403` status code instead of a `500` type of error when unauthorized application requests are identified.
- {: .removed} [LettuceMod](../ticketing/Redis-Ticket-Registry-RediSearch.html) is removed from CAS and its functionality is directly provided by Lettuce itself.

{% include release-footer.html %}
