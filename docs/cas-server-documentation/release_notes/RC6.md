---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC6 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks,
statistics or completion of features or bug fixes. To gain confidence in a particular
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
- [Support](https://apereo.github.io/cas/Support.html)

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `17`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

Please note that CAS CI builds via GitHub Actions are now switched to build against latest EA releases of JDK `21`, in addition to JDK `17`.
Given the release schedule for JDK `21` in September 2023, it is likely that CAS `v7` would switch to using JDK `21` as its baseline
in the next few release candidates and likely for the final GA release later in the year.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
  
### Google Cloud Pub/Sub Ticket Registry

A new ticket registry implementation backed by [Google Cloud's PubSub](../ticketing/GCP-PubSub-Ticket-Registry.html) is now available.
  
### OpenID Connect Dynamic Registration

Supported grant types and response types are recognized during [OpenID Connect Dynamic Registration](../authentication/OIDC-Authentication-Dynamic-Registration.html). 
Furthermore, sensible defaults would be used if grant types or response types are not explicitly requested.

### Feature Removals

Modules, features and plugins that support functionality for Apache CouchDb or Couchbase, previously deprecated, are now removed.
If you are currently using any of these plugins or features, we recommend that you consider a 
better alternative or prepare to adopt and maintain the feature on your own.

### Testing Strategy

The collection of end-to-end browser [tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `403` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Spring Boot

CAS has switched and upgraded to Spring Boot `3.1.x`, presently in milestone/release-candidate mode, and one that is 
anticipated to be released around mid May 2023. It is unlikely that CAS `7.0.x` would be released prior to that date, and 
we intend to take advantage of this time window to run integration tests against the next Spring Boot release. 

### Google Cloud Firestore 

- A new ticket registry implementation backed by [Google Cloud's Firestore](../ticketing/GCP-Firestore-Ticket-Registry.html) is now available.
- A new service registry implementation backed by [Google Cloud's Firestore](../services/GCP-Firestore-Service-Management.html) is now available.

### Google Cloud Logging

Logging support for [Google Cloud Logging](../logging/Logging-GoogleCloud.html) based on Apache Log4j is now available.

### Service Management & Indexing

Registered service types are now internally indexed by the services management facility to assist with advanced and faster querying operations.
Changes in this area allow the underlying service management APIs to efficiently query for a service by a given field such as `clientId`
without having to loop through all registered services. Available indexes include `id`, `name`, `serviceId`, `clientId`, etc.

Indexed fields are generally expected to be uniquely defined. This means that if you have multiple registered services sharing the same
`clientId` or `name` or `id` fields, you most likely will into issues and the service registry may not be able to respond back with the correct
registered service. Review your catalog of registered applicationd with CAS and ensure each definition is assigned to unique values for said fields.

<div class="alert alert-info">:information_source: <strong>Client IDs</strong><p>Remember that client ids assigned to
OAuth and OpenID Connect service definitions are expected to be globally unique. Per specifications, comparisons are handled in a case-sensitive manner.
If you have a client id defined as <code>abc</code> and an authorization request supplies <code>AbC</code>,
CAS may not be able to correctly find the appropriate service definition linked to the requested client id.</p></div>

### Groovy Audits

[Groovy Audits](../audits/Audits-Groovy.html) are now available which can, in small 
and modest ways, control the output and format of the logged auditable record.

### Redis Connection Pooling

By default and in previous releases, multiple Redis connections share a single thread-safe native connection. If you enable connection pooling,
then native connection sharing will be disabled and the connection pool will be used for all operations. You may however explicitly control connection 
sharing via this setting as an override. Furthermore, Redis `SCAN` operations are reviewed across the codebase to ensure connection cursors
are properly closed and returned back to the connection pool.

### Monitoring & Observations

The following operations are now *observed* using [Micrometer Observations](https://micrometer.io) and then reported as metrics:

- Ticket registry operations and queries
- Service management operations and queries
- SAML2 service provider metadata resolution
- Authentication attempts and transactions

In the same category, 

- Integration with [Elastic APM](../monitoring/Configuring-Monitoring-ElasticAPM.html) is now available to monitor CAS component execution and register 
  execution spans and traces with Elastic APM.
- Integration with [Sentry](../logging/Logging-Sentry.html) is now available to monitor CAS component execution and register
  execution spans and traces with Sentry.

### CAS Initializr

Several new enhancements are now offered by the [CAS Initializr](../installation/WAR-Overlay-Initializr.html):

- The service is now prepped to present a starter test suite based on Puppeteer.
- It is able to generate overlay projects based on correct and supported versions of the Gradle build tool that is compatible with each CAS version.
- There is an option to support the [CAS Commandline Shell](../installation/Configuring-Commandline-Shell.html).
- Additional tasks are defined inside the generated CAS overlay to verify CAS ticket registry functionality in high-availability deployments.
- There is an option to support relevant configuration files for deployments that are on GitHub Actions. 
- There is an option to control the deployment type of the generated web application.

### Docker Integration Tests

The following Docker images, used for integration tests, are now upgraded:

- Localstack
- Apache Cassandra
- Amazon DynamoDb
- Google Cloud CLI
- Grouper
- InfluxDb
- MariaDb
- MySQL
- OracleDb
- Grouper
- Redis

## Other Stuff

- JSON and YAML service registries are able to auto-organize and store service definition files in dedicated directories identified by the service type.
- Support for additional settings such as `cluster`, `family`, etc to assist with Hazelcast discovery when CAS is deployed in AWS.
- Minor fixes to encode URL fragments prior to form submission and to allow ticket validation attempts to ignore fragments in the URL.
- `assertionAudiences` defined for SAML2 service definition will now override the default entity id, when defined.
- All DynamoDb integrations can now be configured to take advantage of in-memory acceleration with DynamoDB Accelerator (DAX).
- Several bug fixes and enhancements to correct the [WebAuthN FIDO2](../mfa/FIDO2-WebAuthn-Authentication.html) registration and authentication flows.
- Additional settings for [Impersonation via LDAP](../authentication/Surrogate-Authentication-Storage-LDAP.html) to verify the existence of the surrogate account in the LDAP directory.

## Library Upgrades
       
- Spring  
- Spring Integration
- Netty
- Logback 
- Ldaptive
- Twillio
- jQuery
- Amazon SDK
- MariaDb
- Hazelcast
- Joda-Time
- Spring Boot
- Azure CosmosDb
- Grouper Client
- Spring Cloud
- Swagger
- Spring BootAdmin
- Slf4j
- PostgreSQL
- Gradle
- Thymeleaf Dialect
- Netty
- FontAwesome
- Duo Security
- Spring Integration
- Apache Tomcat
- Kryo
- FIDO2 WebAuthN
