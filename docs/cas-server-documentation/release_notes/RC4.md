---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC4 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note 
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks, statistics 
or completion of features. To gain confidence in a particular release, it is strongly recommended that you start 
early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you
to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support,
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs and
operates. If you consider your CAS deployment to be a critical part of the identity and access management ecosystem, this is a viable option
to consider.

## Get Involved

- Start your CAS deployment today. Try out features and [share feedback](/cas/Mailing-Lists.html).
- Better yet, [contribute patches](/cas/developer/Contributor-Guidelines.html).
- Suggest and apply documentation improvements.

## Resources

- [Release Schedule](https://github.com/apereo/cas/milestones)
- [Release Policy](/cas/developer/Release-Policy.html)

## Overlay

In the `gradle.properties` of the [CAS WAR Overlay](../installation/WAR-Overlay-Installation.html), adjust the following setting:

```properties
cas.version=6.6.0-RC4
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the 
minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
    
### Jakarta API Migrations

The following projects, used internally by CAS, are now converted to use the Jakarta APIs:

- [Person Directory](https://github.com/apereo/person-directory)
- [Inspektr](https://github.com/apereo/inspektr)
- [Spring Webflow](https://github.com/apereo/spring-webflow)
              
For some projects, the migration also upgrades the baseline Java requirement to JDK `17` and 
moves the project to use the Spring framework `6.x` as necessary. 

The Pac4j project has also published [relevant artifacts](https://www.pac4j.org/blog/jakartaee_is_coming.html) based on Jakarta APIs. 

<div class="alert alert-info">
<strong>Note</strong><br/>The CAS project is not switching to Jakarta APIs just yet; this is just to ensure
that the fundamentals and dependant libraries are prepared to handle the migration, when the time comes.
The impact of the migration would be minimal to the end-user configuration, except perhaps when it comes to 
deployment environment requirements such as JDK and servlet containers. Additional details would 
be published as necessary in due time.
</div>

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover 
more use cases and scenarios. At the moment, total number of jobs stands at approximately `303` distinct 
scenarios. The overall test coverage of the CAS codebase is approximately `94%`.

### Single Sign-on Sessions

The following ticket registry implementations are enhanced to provide dedicated native queries
and support for fetching active single sign-on sessions for a given username.
                                                                                
- [MongoDb Ticket Registry](../ticketing/MongoDb-Ticket-Registry.html)
- [Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html)
- [JPA Ticket Registry](../ticketing/JPA-Ticket-Registry.html)
- [DynamoDb Ticket Registry](../ticketing/DynamoDb-Ticket-Registry.html)
- [Hazelcast Ticket Registry](../ticketing/Hazelcast-Ticket-Registry.html) using [Hazelcast Jet](https://jet-start.sh/)

<div class="alert alert-info">
<strong>Note</strong><br/>Remember that all other registry options support this capability.
However, their execution and performance may not be as ideal as those that support dedicated queries.
</div>

Dedicated queries should lead to better performance, though the results may not be immediately obvious
specially if/when the ticket registry is configured to sign and encrypt tickets.

### Deprecated Modules

The following modules are deprecated and scheduled to be removed in future CAS versions:

- [Infinispan Ticket Registry](../ticketing/Infinispan-Ticket-Registry.html)
- [Couchbase Ticket Registry](../ticketing/Couchbase-Ticket-Registry.html)
- [SwivelSecure Multifactor Authentication](../mfa/SwivelSecure-Authentication.html)

### Log Message Sanitation

Ticket identifiers included in CAS log messages are typically sanitized and obfuscated prior to the logging task. In this release,
this behavior applies to all ticket definitions that are registered with the CAS ticket catalog. The syntax of the obfuscated ticket
in the log message is also slightly changed to hide a few more characters in the ticket identifier.
   
### Passwordless Authentication

[Passwordless authentication](../authentication/Passwordless-Authentication.html), when it comes to handling and suppporting delegation,
is now able to decorate the passwordless account with a list of authorized identity providers, and is able to handle auto redirects correctly
when there are multiple candidate identity providers.

### Refreshable JDBC/JPA Integrations

CAS modules that offer an integration with a relational database, such as [JPA Service Registry](../services/JPA-Service-Management.html), 
are now extensively enhanced and tested to ensure the underlying components can correctly respond to *refresh* events, when 
the application context is reloaded once a property is changed.

### Simple Multifactor Authentication

Operations that handle token management, validation and generation for 
the [Simple Multifactor Authentication](../mfa/Simple-Multifactor-Authentication.html) 
flow can now be outsourced to an external REST API, and need not be controlled or owned by CAS. 
     
### OpenID Connect Dynamic Client Registration

When using [Dynamic Client Registration](../authentication/OIDC-Authentication-Dynamic-Registration.html) for OpenID Connect
in `PROTECTED` mode, the registration process now requires an initial access token that is specially authorized to make registration
requests, where this token can be obtained via the `/oidc/initToken` endpoint.

### CAS Commandline Shell

[CAS Commandline Shell](../installation/Configuring-Commandline-Shell.html) is now able to generate 
signed JWTs using a given JSON Webkeystore.
  
### Pattern Matching Attribute Release Policy

Attribute bundles can now be released to application using a *pattern matching* policy
that primarily operates on the attribute's value(s), and optionally applies transformation rules
on each value before assembling the final result. [See this guide](../integration/Attribute-Release-Policies.html) for more.

### Integration Testing & Docker Images

The following Docker images, used for integration testing, are now upgraded to their latest version:

- AWS Localstack
- Apereo CAS
- Couchbase
- Apache Cassandra
- Apache CouchDb
- Apache Syncope
- InfluxDb
- MariaDb
- MongoDb
- MySQL
- PostreSQL
- Redis
- SCIM

## Other Stuff
            
- The token introspection for [OAuth](../protocol/OAuth-Protocol.html) and [OpenID Connect](../protocol/OIDC-Protocol.html) is now fixed to show the correct 
  response for invalid/unknown tokens.
- Multifactor authentication with [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) can now support a customizable identifier and does 
  not have to be limited to `mfa-gauth`.
- Logout operations for [delegated authentication](../integration/Delegate-Authentication.html) are now disabled and skipped if single logout is disabled.

## Library Upgrades

- InfluxDb
- Amazon SDK
- ErrorProne
- Gradle
- Hazelcast
- JGit
- MariaDb Driver
- PostgreSQL Driver
- Amazon SDK
- Spring Cloud
- Mockito
- Groovy
- Nimbus OIDC
- Font Awesome
- Swagger
- Micrometer
- Netty
- Twilio
- Spring
- Spring Boot
