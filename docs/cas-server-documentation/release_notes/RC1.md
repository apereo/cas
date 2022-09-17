---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC1 Release Notes

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
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs and
operates. If you consider your CAS deployment to be a critical part of the identity and access management ecosystem, this is a viable option to consider.

## Get Involved

- Start your CAS deployment today. Try out features and [share feedback](/cas/Mailing-Lists.html).
- Better yet, [contribute patches](/cas/developer/Contributor-Guidelines.html).
- Suggest and apply documentation improvements.

## Resources

- [Release Schedule](https://github.com/apereo/cas/milestones)
- [Release Policy](/cas/developer/Release-Policy.html)

## New & Noteworthy

The following items are new improvements and enhancements presented in this release. 
   
### JDK Requirement

The JDK baseline requirement for this CAS release is and **MUST** be JDK `17`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover more use cases 
and scenarios. At the moment, total number of jobs stands at approximately `330` distinct scenarios. The overall 
test coverage of the CAS codebase is approximately `94%`.

### Removed Modules

The following modules that were previously marked as deprecared are now removed from CAS and will no longer
be supported, maintained or released:

- OpenID Protocol
- Digest Authentication
- Apache Shiro Authentication
- Apache Fortress Authentication
- Ehcache Ticket Registry
- SwivelSecure Multifactor Authentication
- Infinispan Ticket Registry
- Couchbase Ticket Registry
- Scripted Attribute Repository
- Scripted Registered Service Attribute Release Policy
- Scripted Username Attribute Provider
- NTLM Authentication
- SCIM v1 Provisioning 

### Conditional Access Strategy

The ABAC authorization policy assigned to a registered service can 
now be [conditionally activated](../services/Service-Access-Strategy-ABAC-Activation.html).

### SAML2 Metadata Resolution

In the event that a SAML2 service provider is configured to download metadata from a URL, CAS may now attempt to reuse the previously-downloaded
backup file on disk for the service provider, if the metadata file is still seen as valid. This capability will require the forceful fetching
of the metadata over HTTP to be disabled.
 
### Docker Images

A number of Docker images used for integration testing are now upgraded to their latest available versions:

- LocalStack
- Apereo CAS
- Apache Cassandra
- Couchbase Server
- Amazon DynamoDb
- InfluxDb
- MariaDb
- MongoDb
- MySQL
- PostgreSQL
- Redis

## Other Stuff
            
- [Registered Service ABAC policy](../services/Service-Access-Strategy-ABAC.html) can now support inline Groovy conditions for attributes.
- [CAS configuration security](../configuration/Configuration-Properties-Security-CAS.html) via Jasypt is able to respect the IV 
  generation flag based on algorithms.
- Embedded YAML application configuration files are able to override default application properties that ship with CAS.
- Authentication context classes in the SAML2 response can be determined from context mapping settings when no specific context class is present in the 
  SAML2 authentication context.
- SAML2 metadata resolution for service providers are now sent to the CAS audit log and recorded under `SAML2_METADATA_RESOLUTION`.
- Support for [Google Analytics 4](../integration/Configuring-Google-Analytics.html) is now included.
- [Auditable CAS events](../audits/Audits.html) are now automatically collected and displayed in the documentation.

## Library Upgrades

- Spring 
- Spring Boot
- Mockito
- Nimbus
- Twilio
- Netty
- JGit
- Spring Cloud
- Pac4j
- Couchbase Client
- Micrometer
