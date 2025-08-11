---
layout: default
title: CAS - Release Notes
category: Planning
---

{% include variables.html %}

# 7.3.0-RC4 Release Notes

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

The JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Password Management via Apache Syncope

[Apache Syncope](../password_management/Password-Management-ApacheSyncope.html) support is now 
able to handle password management operations. [Syncope Authentication](../authentication/Syncope-Authentication.html) 
is also improved to detect user account statuses that may be suspended or require password changes. Furthermore,
Apache Syncope is now able to supply configured security questions to CAS, and exposes the ability to validate
the security answers provided by the user during password management operations. Finally, this integration
is now allowed to fetch security questions from Apache Syncope and have them be validated against the user's
profile defined in Apache Syncope.

### Password Management Principal Resolution

Password management operations that require access to the user's email address, phone, etc
will now attempt to resolve the user's attributes using the configured principal resolution strategy
and defined attribute repositories before outsourcing that task to the password management interface.
  
### Session Management via Ticket Registry

You may already know that HTTP session replication may be required for clustered deployments that take advantage of specific CAS
modules and features that store data into the container's HTTP session. As of this writing, one such example
would be [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html).

In addition to the usual direct options provided by [Spring Session](../webflow/Webflow-Customization-Sessions-ServerSide.html), CAS now 
supports the ability to store HTTP sessions in the [Ticket Registry](../webflow/Webflow-Customization-Sessions-ServerSide-TicketRegistry.html).
Doing so allows you to take advantage of the existing ticket registry storage backends and removes the need to configure
sticky sessions in your load balancer or application server, etc.

### Session Management Deprecations

Per the above note, some CAS features and modules at the moment define and provide their own options and settings to
replicate HTTP sessions in a clustered environment. All such options, features and settings are now deprecated
and will be removed in future CAS releases. A few examples of such options and features include:

- `cas.authn.saml-idp.core.session-storage-type=TICKET_REGISTRY`
- `cas.authn.saml-idp.core.session-replication.cookie.*`
- `cas.authn.oauth.session-replication.replicate-sessions`
- `cas.authn.oauth.session-replication.cookie.*`
- `cas.authn.pac4j.core.session-replication.replicate-sessions`
- `cas.authn.pac4j.core.session-replication.cookie.*`
- `cas.session-replication.cookie.*`
- `cas.authn.pac4j.core.session-replication.cookie.*`
- ...more...
    
As noted above, session replication can still be handled via the 
[Ticket Registry](../webflow/Webflow-Customization-Sessions-ServerSide-TicketRegistry.html)
which is done as a direct and native integration with the Spring Session library. 
        
If you're using the above options and features, it is recommended that you start using the above session replication strategy
and remove any existing configuration that uses the above options. Future CAS releases will
**remove such options** and all components and features that make use of the listed options.
  
### Theme Changes

In anticipation of Spring Framework `v7` and Spring Boot `v4`, a large number of deprecated APIs are now 
internally removed and reworked in the CAS codebase. A significant number of such changes deal with theme management
and the way themes are defined, loaded and processed in the CAS user interface views backed by Thymeleaf. 
While such changes are not expected to impact existing themes and pages yet, we recommend that you review your current HTML pages
and specifically replace anything that references `#themes.code(...)` with `#cas.theme(...)`. This change will 
put you in a better position to upgrade to future CAS releases that will remove the deprecated APIs and references.
 
### Configuration Properties Validation

On startup, CAS will now validate the configuration properties somewhat more aggressively and in particular
will output warnings and messages for properties that are either removed or marked as deprecated and scheduled for removal.
Where applicable, replacements will be suggested to help you migrate your configuration to the closest alternative.
The changes here extend and enhance the functionality offered by Spring Boot configuration metadata and migration process.
 
### OpenID Connect Attribute Definitions

If you have configured [attribute definitions](../integration/Attribute-Definitions.html) 
that are used to map OpenID Connect claims or OAuth attributes, you need to
adjust your configuration to use more dedicated types. In particular, OAuth attribute definitions now gain their own
dedicated type via `org.apereo.cas.support.oauth.profile.OAuth20AttributeDefinition` and just as before, OpenID Connect
attribute definitions are defined under `org.apereo.cas.oidc.claims.OidcAttributeDefinition`. This separation is done to avoid
conflicts and not mix concerns, allowing CAS to process the same attribute definition for different protocols.

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
and scenarios. At the moment, total number of jobs stands at approximately `526` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

## Other Stuff
     
- Integration tests have switched to use Redis `v8.2`.
