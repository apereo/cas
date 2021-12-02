---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC3 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting
for a `GA` release is only going to set you up for unpleasant surprises. A `GA`
is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS
releases are *strictly* time-based releases; they are not scheduled or based on
specific benchmarks, statistics or completion of features. To gain confidence in
a particular release, it is strongly recommended that you start early by
experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we
invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your
deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings
attached. Having the financial means to better sustain engineering activities will allow
the developer community to allocate *dedicated and committed* time for long-term
support, maintenance and release planning, especially when it comes to addressing
critical and security issues in a timely manner. Funding will ensure support for
the software you rely on and you gain an advantage and say in the way Apereo, and
the CAS project at that, runs and operates. If you consider your CAS deployment to
be a critical part of the identity and access management ecosystem, this is a viable option to consider.

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
cas.version=6.5.0-RC3
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
     
### Spring Boot `2.6.x`

CAS has now switched to use the Spring Boot `2.6.x` release line, and all other relevant dependencies
such as Spring Cloud, Spring Data, Spring Security, etc have also been upgraded. While this is a somewhat significant
upgrade, its effects and consequences should largely remain invisible to the end-user. Aside from all the usual 
reasons, this upgrade should allow CAS to be one step closer to native builds using the likes of GraalVM. 

<div class="alert alert-info">
<strong>Remember</strong><br/>Be sure to review your CAS Overlay configuration
to make sure the Spring Boot version correctly matches that of CAS. Creating a CAS Overlay
project using the CAS Initializr service should already account for this change.
</div>

This upgrade affects CAS via the following ways:

- By default, circular `@Bean` references are no longer allowed by Spring Boot. The only module affected by this change is the CAS Command-line Shell.
- The default MVC path matcher is set as `spring.mvc.pathmatch.matching-strategy=ant-path-matcher` to restore and enforce current behavior, particularly for OpenID Connect endpoints.

### Account (Self-Service) Registration

CAS provides a modest workflow to 
handle [self-service account registration](../registration/Account-Registration-Overview.html).
This capability was developed and first released in `6.5.0-RC1`, and it will be repeatedly refined
and improved in the future to match and accommodate realistic workflows deployed today as much as possible. 

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to add additional scenarios. At this point, there are
approximately `215` test scenarios and we'll continue to add more in the coming releases.

### OpenID Connect Key Rotation
     
CAS can now be configured to rotate keys in the [OpenID Connect](../authentication/OIDC-Authentication-JWKS.html) 
keystore automatically based on a predefined schedule. Rotation will include previous keys as well as current and future
keys to assist with integrations and caching concerns. There is also a revocation schedule for old
inactive keys that should be removed from the keystore.

### Chained Service Access Strategies

[Service access strategies](../services/Configuring-Service-Access-Strategy.html) can now 
be chained and grouped together to deliver advanced conditions
and grouping logic using multiple `AND` or `OR` rules.
  
### OpenID Connect JWKS Storage

The generation of the [JWKS resource for OpenID Connect](../authentication/OIDC-Authentication-JWKS.html) 
can now be outsourced to external REST APIs, Groovy scripts or relational databases. Each storage service implementation
should also be able to support key rotation and revocation as is handled by CAS itself.

### Audit Log Data Structure

Audit log records and storage services are now modified to include user agent information using the `User-Agent` header.
This *might* be a breaking change, particularly for relational databases that have a fixed table structure. 
If you are not allowing CAS to update database schemas automatically, you will need to ensure the audit log table
contains a `AUD_USERAGENT` database column, preferably set to `varchar(length = 512)`.

## Other Stuff
            
- Generation of [SAML2 IdP Metadata](../installation/Configuring-SAML2-DynamicMetadata.html) is now postponed until the application ready event.
- Minor performance improvements to assist with locating SAML2 services in the service registry.
- Possible X509 stack overflow errors when fetching X509 certificates from headers are now fixed. 
- All Redis integrations are now able to support TLS options for encrypted connections and transports.
- All Hazelcast integrations are now able to support TLS options for encrypted connections and transports.
- DynamoDb tables names that affect OAuth and OpenID Connect functionality are now customizable via CAS settings.
- Cache invalidation rules for static resources such as CSS/JS files using `ResourceUrlProviderExposingInterceptor` is now restored.
- When performing a logout after an OIDC login process, the OIDC session is now properly removed when it is distributed.
- [Authy MFA](../mfa/AuthyAuthenticator-Authentication.html) configuration is now conditionally activated when Authy API keys are found in CAS configuration.
- [Passwordless LDAP](../authentication/Passwordless-Authentication-Storage-LDAP.html) configuration is activated when an LDAP url is found in configuration.
- Identifiers of consent decisions are now fixed to be operable using the consent actuator endpoint. These IDs are now serialized as `String` to avoid rounding issues with long numbers during JSON serialization.

## Library Upgrades

- Spring Boot
- Spring Data
- Spring Security
- Spring Cloud
- Micrometer
- Amazon SDK
- SpotBugs
- Spring Session
- Spring Framework
- MongoDb Driver
- Azure CosmosDb
- Spring Boot Admin
- Pac4j
- Bucket4j
- Maxmind
- Apache Tomcat
- Thymeleaf
- Okta SDK
- BouncyCastle
- Twilio
- Gradle
