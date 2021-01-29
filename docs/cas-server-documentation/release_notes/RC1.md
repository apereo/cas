---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC1 Release Notes

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
cas.version=6.4.0-RC1
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the 
minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### CAS Documentation

CAS documentation has gone through a cleanup effort to improve how configuration settings are 
managed and presented. Configuration namespaces for CAS settings are presented as individual
snippets and fragments appropriate for each feature, and are included throughout the documentation
pages where necessary, split into panes for required, optional and third-party settings, etc. 

The presentation and generation of CAS settings and their documentation is entirely driven by CAS configuration metadata,
and this capability is ultimately powered by Github Pages and Jekyll that render the CAS documentation in the backend.

Please note that as part of this change, a number of CAS configuration settings are moved around into new namespaces
to make the generation of configuration metadata and relevant documentation snippets easier. Most likely, settings
are moved into a new `.core.` or `.engine.` namespace. Some of the settings that are affected by this effort
are: 

- `cas.authn.oidc`
- `cas.authn.oauth.uma`
- `cas.authn.saml-idp`
- `cas.events`
- `cas.acceptable-usage-policy` 
- `cas.ticket.registry.hazelcast` 
         
The change has a number of major advantages when it comes to maintainability and correctness of documentation:

- Configuration settings no longer need to be manually documented. If a setting is removed, renamed or updated in any way in the CAS codebase, its relevant reference in the documentation will be automatically updated
- The documentation of each setting is directly extracted from the source code and the Javadoc for the field itself. If a setting is owned by a third-party library, its explanation no longer needs to be duplicated in the CAS documentation.
- If a setting does not present any or adequate documentation, you're advised and encouraged to find the relevant source and update its documentation in form of a contribution or pull request, whether it's owned by CAS or some other third-party library.

### Spring Boot 2.4
                  
CAS is now based on the Spring Boot `2.4.x` series which by extension also requires CAS to upgrade
its dependency on related projects such as [Spring and Spring Cloud](../planning/Architecture.html). While this is a 
significant framework upgrade, the change should remain largely invisible to CAS users and adopters.

### Testing Strategy

Th collection of [end-to-end browser tests](../developer/Test-Process.html) based on Puppeteer 
continue to grow to add additional scenarios and use cases, such as recaptcha, logout, and more.
Furthermore, the CAS codebase is now running its entire tests suite against Ubuntu, Windows and MacOS
platforms. New test categories are also added to account for SAML2 integration tests both as an
identity provider and service provider, along with tests specific OpenID Connect authentication
flows for `code`, `token`, `id_token` and more.
  
### Logout Confirmations

Upon [logout confirmations](../installation/Logout-Single-Signout.html), the 
CAS user interface and confirmation screens are 
now able to list all applications linked to the existing SSO session.

### reCAPTCHA Integrations

[Password Management](../password_management/Password-Management.html) extended 
to offer a reCAPTCHA integration for its *Forgot Username* feature. This 
change also separates the reCAPTCHA configuration namespace,
allowing each CAS feature (login, password rest, etc) to separately own, control 
and modify reCAPTCHA settings.

### XML-less Spring Webflow

The construction of various [Spring Webflow flows](../webflow/Webflow-Customization.html) and (multifactor authentication) subflows 
has now removed the requirement for an XML foundation, allowing the construction of all flows to be dynamic.

### AWS SQS Logging

A dedicated logging appender is now available to support routing logs to [AWS SQS](../logging/Logging-SQS.html).

### WebAuthN REST Device Management

[WebAuthN/FIDO2 Device registrations](../mfa/FIDO2-WebAuthn-Authentication.html) 
may be managed using an external REST API.

### Scriptable LDAP Queries

Search filters used to query LDAP for results can now be designed as Groovy scripts
to provide dynamic querying options.

### Inwebo MFA Integration

Support for [inWebo](../mfa/Inwebo-Authentication.html) as a multifactor authentication provider is now available.
 
### SCIM Provisioning

Integration with [SCIM Provisioning](../integration/SCIM-Integration.html) is slightly
improved to allow SCIM targets and settings per registered application.

### WebAuthN Redis Device Management

[WebAuthN/FIDO2 Device registrations](../mfa/FIDO2-WebAuthn-Authentication.html)
may be managed using a Redis database.

### Grouper Access Strategy

[Grouper Service Access Strategy](../services/Configuring-Service-Access-Strategy.html) can 
now accept configuration properties 
in the service definition to override the default Grouper settings.

### CORS Per Service

[CORS configuration](../services/Configuring-Service-Http-Security-Headers.html) and 
relevant headers, etc can now be defined on a per-application basis.

### SAML2 Metadata via Redis

Metadata artifacts that belong to CAS as a SAML2 identity provider, as well as metadata
for service providers can now be [managed via Redis](../installation/Configuring-SAML2-DynamicMetadata.html).

### ACME & Let's Encrypt

CAS can now enable support for the [(ACME) protocol](../integration/ACME-Integration.html) 
using a certificate authority (CA) such as Let's Encrypt.

### Gradle 6.8

The CAS codebase is now using Gradle version `6.8` for internal builds. All plugins and build deprecation warnings
are adjusted, fixed or removed to make for a smooth transition to version `7` as the next release of the Gradle build tool.

### Multifactor Trusted Devices via Redis 

[Multifactor Trusted Devices](../mfa/Multifactor-TrustedDevice-Authentication.html) and user 
decisions for multifactor authentication may also be kept inside a Redis instance.

## Other Stuff
     
- [Hazelcast cluster configuration](../ticketing/Hazelcast-Ticket-Registry.html) allows specification of network interfaces.
- Delegated authentication configuration can allow for a pre-defined callback/redirect URI.
- [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) now includes a few additional expressions common for date/time operations.
- Configuration metadata is corrected in a number of cases to make sure `@NestedConfigurationProperty` is properly set on fields.
- Publishing Maven metadata into the local maven repository is corrected to include all CAS-required repositories.
- [Audit logs](../audits/Audits.html) attempt to produce syntactically correct JSON output for certain resources rather than relying on `toString()` methods for nested objects, when audit format is configured to produce JSON payloads.
- [REST Service Registry](../services/REST-Service-Management.html) has changed its `DELETE` specification, and will use the service numeric identifier as a path variable for expected delete operations. 
- [Time-based service access strategy](../services/Configuring-Service-Access-Strategy.html) can now take advantage of [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) for its configuration.

## Library Upgrades

- Spring Framework
- Spring Boot
- Spring Cloud
- Hibernate
- Amazon SDK
- Hazelcast
- Joda Time
- Gradle  
- HikariCP
- CouchDb Client
- Thymeleaf Dialect
- Apache Ignite
- BouncyCastle
