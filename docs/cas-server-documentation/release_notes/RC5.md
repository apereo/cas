---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC5 Release Notes

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

The JDK baseline requirement for this CAS release is and **MUST** be JDK `17`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
  
### Redis Ticket Registry
    
The implementation of the Redis ticket registry has internally changed its data structure to allow for proper indexing of
ticket documents and their fields to assist with full-text searching. The registry implementation is now able to recognize the
presence of [RediSearch](https://redis.io/docs/stack/search/) module and create appropriate indexes to allow for subsequent search queries.
While ReidSearch is not a requirement and the deployment should be able to proceed without it, its presence should greatly 
improve the performance of ticket registry queries that attempt to look up tickets by attributes.

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `395` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Apache Cassandra Ticket Registry

The implementation of the [Cassandra ticket registry](../ticketing/Cassandra-Ticket-Registry.html) 
has internally changed its data structure to allow for proper indexing of
ticket documents and their fields to assist with full-text searching. 

### OpenID Connect Claim Mappings

[Remapping OpenID Connect claims](../authentication/OIDC-Authentication-Claims.html) at the service level is now able to reprocess and rebuild
claim definitions using inline Groovy scripts. This in particular might be useful when there are custom
user-defined scopes that contain non-standard claims that need to be rebuilt off of existing attributes for a specific service.
            
### Template Service Definitions

A registered service template definition is the foundation and initial building block to construct a service definition
and may act as a pattern of all future service definition to reduce duplication and assist with maintenance and sharing.
To learn more, please [see this](../services/Configuring-Service-Template-Definitions.html).

### Apache Ignite Ticket Registry

The implementation of the [Apache Ignite ticket registry](../ticketing/Cassandra-Ticket-Registry.html)
has internally changed its data structure to allow for proper indexing of
ticket documents and their fields to assist with full-text searching.

### Linked Attributes & Release Policy

A [new attribute release policy](../integration/Attribute-Release-Policy-ReturnLinked.html) is now available 
that can be linked to a collection of attributes to treat them as a source of values on a per-application basis.
                                                                                                 
### Gradle Configuration Cache

Adjustments to the CAS Gradle build now allow the build process to turn on the Gradle configuration cache. The configuration cache is a feature that
significantly improves build performance by caching the result of the configuration phase and reusing this for subsequent builds. The same change is
also enabled and activated for all [CAS Overlays](../installation/WAR-Overlay-Installation.html) that are produced by the CAS Initializr.

Note that not all build plugins may support the Gradle configuration cache. If you run into issues, you can always disable the cache in your `gradle.properties`
file via the following setting: 

```properties
org.gradle.unsafe.configuration-cache=false
```

### OpenID Connect ID Token Expiration Policy

The expiration policy of [OpenID Connect ID tokens](../authentication/OIDC-Authentication-TokenExpirationPolicy.html) can 
now be defined on a per-application basis.

### Feature Deprecations

Modules, features and plugins that support functionality for Apache CouchDb or Couchbase are now deprecated.
These components are scheduled to be removed in a future CAS version and will no longer be maintained or supported. If you are currently using any of these 
plugins or features, we recommend that you consider a better alternative or prepare to adopt and maintain the feature on your own. 

### Feature Removals
 
The following deprecated features and settings are now removed from the CAS codebase:

- Custom components used to provide or validate SAML2 tokens when CAS is configured to support [the WS Federation Protocol](../protocol/WS-Federation-Protocol.html). These components were only supplied to support OpenSAML v4 APIs and were deprecated in CAS `6.6.0`.
- The *Legacy* strategy used to generate device record keys for trusted devices in a multifactor authentication flow. This strategy was deprecated in CAS `6.2.0`
- The `requiredHandlers` setting assigned to a registered service definition. This setting was deprecated in CAS `6.2.0`.

### Google Cloud (GCP) Secret Manager

[Google Cloud Secret Manager](../configuration/Configuration-Server-Management-SpringCloud-GCP-SecretManager.html) can 
now be used as a configuration source for CAS properties and settings. 

### Attribute Definition Enhancements

Attributes registered in the [CAS attribute definition store](../integration/Attribute-Definitions.html) are now able to build values
using regular expressions and pattern matching. Moreover, definitions are also allowed to flatten their values using an assigned delimiter.

## Other Stuff
        
- Locating SAML2 assertion consumer service URLs in the metadata is handled via a case-insensitive strategy.
- Basic support for routing logs to [Fluentd](../logging/Logging-Fluentd.html) is now available.
- The `jwksCacheDuration` is able to support the duration syntax for OpenID Connect services, removing the need to specify a time unit separately.
- The `aud` claim for OpenID Connect ID tokens or JWT access tokens can now be [controlled for each application](../authentication/OAuth-Authentication-Clients.html).

## Library Upgrades

- Apache Kafka
- Gradle
- MongoDb
- Caffeine
- FontAwesome
- Micrometer
- Spring Boot
- Spring
- Hazelcast
- WebAuthN
- Nimbus JOSE
- Duo Security
- Lettuce
- SCIM
- PostgreSQL
- Amazon SDK
- Inspektr
