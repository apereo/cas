---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.1.0-RC5 Release Notes

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
- [Maintenance Policy](/cas/developer/Maintenance-Policy.html)

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html).
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based via Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `482` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.
  
Furthermore, the following docker images used for integration testing are now upgraded:

- Localstack
- Apereo CAS
- DynamoDb
- Elastic APM
- GCP
- Grouper
- InfluxDb
- Kafka
- MariaDb
- PostgreSQL
- Redis

### CAS Initializr SBOM Support

CAS Initializr is now modified to generate a Software Bill of Materials (SBOM) using the CycloneDX format. This SBOM can be used to
track and manage the open-source components used in your CAS deployment and may be examined via the `sbom` actuator endpoint.
   
### Cloudflare Turnstile

Cloudflare Turnstile is a free tool to replace CAPTCHAs and delivers frustration-free, CAPTCHA-free web experiences 
to website visitors. CAS now supports Cloudflare Turnstile as a [CAPTCHA option](../integration/Configuring-Google-reCAPTCHA.html).

### Encryption Algorithm

The default content encryption algorithm for crypto operations has now switched from `A128CBC-HS256` to `A256CBC-HS512`, which requires a larger key size for better security.
To continue using your existing keys, you would need to instruct CAS to use the previous algorithm by setting the following property:

```properties
cas.[path-to-configuration-key].crypto.alg=A128CBC-HS256
```

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
This is potentially a breaking change. Make sure to review your configuration and adjust settings accordingly.
</p></div>

### Passwordless Authentication with reCAPTCHA

[Passwordless authentication](../authentication/Passwordless-Authentication.html) can now support reCAPTCHA to protect against automated abuse, 
such as credential stuffing attacks.

### SAML2 Delegated Authentication

All functionality and components that allow CAS to route authentication requests to external SAML2 identity providers are now consolidated under a single module.
This is done to simplify the setup in a more modular way and reduce the number of libraries and dependencies that would be pulled into the build.

Please make sure you review the [SAML2 delegated authentication](../integration/Delegate-Authentication-SAML.html) page and include the correct module in your build.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
This is potentially a breaking change. Make sure to review your build's dependencies and adjust modules to restore functionality.
</p></div>

### OpenID Connect Delegated Authentication

Similar to the above entry, all functionality and components that allow CAS to route authentication requests to external OAuth or OIDC identity providers are now consolidated under a single module.
This is done to simplify the setup in a more modular way and reduce the number of libraries and dependencies that would be pulled into the build.

Please make sure you review the notes referenced [here](../integration/Delegate-Authentication-OAuth20.html) or [here](../integration/Delegate-Authentication-Generic-OpenID-Connect.html) 
and include the correct module in your build. Note that this change affects any external identity provider, (such as GitHub, Facebook, Apple, etc), that uses the OAuth2 or OpenID Connect protocol.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
This is potentially a breaking change. Make sure to review your build's dependencies and adjust modules to restore functionality.
</p></div>

### SAML2 Delegated Authentication Metadata

CAS is now able to store and manage its SAML2 service provider metadata via [Amazon S3 buckets](../integration/Delegate-Authentication-SAML-Metadata.html). 
This feature is relevant only when [SAML2 delegated authentication](../integration/Delegate-Authentication-SAML.html) is turned on.
    
### Apache Groovy & Scripting
                                                                    
Apache Groovy support, that backs all scripting functionality in CAS, is now extracted and moved into its 
[own dedicated module](../integration/Apache-Groovy-Scripting.html). This means that any sort of feature or functionality
that intends to evaluate and execute a *script* of some sort, such as triggering multifactor authentication or releasing attributes, etc 
would need to include the relevant module in the build. 

<div class="alert alert-warning">:warning: <strong>Pay Attention!</strong><p>
If you are using Apache Groovy or scripting functionality in your build, this is a breaking change. Make sure 
to review the CAS documentation and adjust the dependencies in your CAS build to restore functionality.
</p></div>

This extraction is done to reduce the final size of the vanilla CAS web application by about `9MB`. In addition to a leaner binary artifact, 
this will also remove unnecessary optional libraries out of the CAS build, leading to fewer false CVEs, faster startup times and quicker builds
particularly when it comes to [Graal VM native images](../installation/GraalVM-NativeImage-Installation.html).

## Other Stuff

- [Simple Multifactor Authentication](../mfa/Simple-Multifactor-Authentication.html) may prompt the user to proceed to account registration, when no contact information is found.
- ID token `jti` claims in [OpenID Connect](../authentication/OIDC-Authentication.html) are no longer plain ticket (granting-ticket) identifiers but are instead digested using `SHA-512`.
- The `ticketRegistry` [actuator endpoint](../ticketing/Configuring-Ticketing-Components.html) now offers the ability to run the ticket registry cleaner task on-demand.
- Docker Swarm support for Hazelcast has been removed from the CAS codebase. 
- Claims that are collected in a JWT Access Token are now forced to pass through attribute release policies assigned to the application definition.
- WebAuthn registration endpoint is now able to detect and track an authenticated request using the CAS in-progress authentication attempt. 
- Internal data structures used to index registered service definitions for better querying and searching should now prevent duplicate service definitions.
- Background jobs such as the ticket registry cleaner or service registry loaders can now be scheduled via cron expressions.

## Library Upgrades

- Gradle
- AWS SDK
- Spring Shell
- Grouper Client
- Elastic APM
- Spring Boot
- Spring Cloud
- Google Cloud
- Spring Boot Admin
- Spring Integration
- Spring Security
- Spring Data
- Nimbus JOSE JWT
- Spring Session
- Java Melody
- ErrorProne
- Twilio
- Gradle
- Sentry
- Jakarta Servlet API
- Netty
- Amazon SDK
- Pac4j
- Micrometer
- Apache Tomcat
