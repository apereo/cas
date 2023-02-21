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
and scenarios. At the moment, total number of jobs stands at approximately `393` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Apache Cassandra Ticket Registry

The implementation of the [Cassandra ticket registry](../ticketing/Cassandra-Ticket-Registry.html) 
has internally changed its data structure to allow for proper indexing of
ticket documents and their fields to assist with full-text searching. 

### OpenID Connect Claim Mappings

[Remapping OpenID Connect claims](../authentication/OIDC-Authentication-Claims.html) at the service level is now able to reprocess and rebuild
claim definitions using inline Groovy scripts. This in particular might be useful when there are custom
user-defined scopes that contain non-standard claims that need to be rebuilt off of existing attributes
for a specific service.
            
### Template Service Definitions

A registered service template definition is the foundation and initial building block to construct a service definition
and may act as a pattern of all future service definition to reduce duplication and assist with maintenance and sharing.
To learn more, please [see this](../services/Configuring-Service-Template-Definitions.html).

### Apache Ignite Ticket Registry

The implementation of the [Apache Ignite ticket registry](../ticketing/Cassandra-Ticket-Registry.html)
has internally changed its data structure to allow for proper indexing of
ticket documents and their fields to assist with full-text searching.

## Other Stuff
        
- Locating SAML2 assertion consumer service URLs in the metadata is handled via a case-insensitive strategy.
- Basic support for routing logs to [Fluentd](../logging/Logging-Fluentd.html) is now available.
- The `jwksCacheDuration` is able to support the duration syntax for OpenID Connect services, removing the need to specify a time unit separately.
- The `aud` claim for OpenID Connect ID tokens or JWT access tokens can now be [controlled for each application](../authentication/OAuth-Authentication-Clients.html).
- CAS features supporting Couchbase are now deprecated and scheduled to be removed in the future.

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
