---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC3 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set you up for 
unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS releases are *strictly* 
time-based releases; they are not scheduled or based on specific benchmarks, statistics or completion of features. To gain confidence in a particular 
release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed *almost exclusively* on a
voluntary basis with no expectations, commitments or strings attached. Having the financial means to better sustain engineering activities will allow the
developer community to allocate *dedicated and committed* time for long-term support, maintenance and release planning, especially when it comes to
addressing critical and security issues in a timely manner. Funding will ensure support for the software you rely on and you gain an advantage and say in
the way Apereo, and the CAS project at that, runs and operates. If you consider your CAS deployment to be a critical part of the identity and access
management ecosystem, this is a viable option to consider.

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
cas.version=6.6.0-RC3
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Account Profile Management

Devices that are registered with CAS for multifactor authentication flows and integrations can now be listed
in the [account profile dashboard](../registration/Account-Management-Overview.html) page. At the moment,
the supported multifactor providers for this capability
are [Duo Security](../mfa/DuoSecurity-Authentication.html), [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html), 
and [WebAuthn FIDO2](../mfa/FIDO2-WebAuthn-Authentication.html).

<img width="1699" alt="image" src="https://user-images.githubusercontent.com/1205228/164191147-1864c987-a339-4678-98e6-54d2beb8200c.png">

Additionally, [attribute consent decisions](../integration/Attribute-Release-Consent.html) and 
records tied to the authenticated user profile are now displayed in the account profile dashboard.

This mini-portal will be improved in the coming releases to list more account-related data, 
such as one's active single sign-on sessions, etc.
                      
### Spring Boot 2.7
                   
CAS components are now upgraded to use and build against Spring Boot `2.7`. While the upgrade should remain largely invisible, there are changes 
to how auto-configuration components are now constructed and registered with Spring Boot mainly via a new `@AutoConfiguration` annotation. 

> This annotation is now used to annotate top-level auto-configuration classes that are listed in the 
new `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` file, replacing `@Configuration`. Configuration classes that are 
nested within or imported by an @AutoConfiguration class should continue to use `@Configuration` as before.

For backwards compatibility, entries in existing `spring.factories` files will still be honored particularly if you have designed your own configuration 
components. Internal CAS configuration classes will slowly begin to transition to the new `@AutoConfiguration` annotation to prepare for a future upgrade
to Spring Boot `3.0`. This change will eventually become visible in CAS overlays that are built by 
the [CAS Initializr](../installation/WAR-Overlay-Initializr.html).
  
### CAS Registered Services

Application definitions that are registered with CAS typically are marked with `RegexRegisteredService` that indicates the service type. As part of a larger 
refactoring effort to simplify the service definition models and to assist with future development efforts in the area of authorization policies, such 
services should be updated to use the now-dedicated type `CasRegisteredService` for all CAS-enabled applications. 

<div class="alert alert-warning"><strong>Usage</strong>
<p>While the deprecation warning is quite harmless for now, we STRONGLY suggest that you visit 
your application definitions and perform a bulk-update to avoid breaking upgrades in the future.</p>
</div>

An example application definition using the new type follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://github.com/apereo/cas",
  "name" : "CAS",
  "id" : 1
}
```

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover more use cases and scenarios. At the moment, total number of jobs 
stands at approximately `290` distinct scenarios. The overall test coverage of the CAS codebase is approximately `94%`.

### Groovy Webflow Actions

Certain Spring Webflow actions are now given the option for an [alternative Groovy implementation](../webflow/Webflow-Customization-Extensions.html). This 
allows one to completely replace the Java implementation of a Spring webflow action that is provided by CAS with a Groovy script for custom use cases and
total control in scenaios where using Java may not be ideal or possible. As part of this change, all CAS webflow actions should be correctly marked with
`@ConditionalOnMissingBean` annotations that would allow one to customize and replace them with one's own implementation as necessary.
 
### Template Service Definitions

Service definition records such as those that represent SAML2 service providers or OpenID Connect relying parties carry many fields
that are assigned default values. In scenarios where there is no setting to change the field behavior at a global level, the 
alternative might be to update all service definitions to change field default. To accomodate this change, options are now available
to allow changing of the default field values for service definitions. This enhancement is initially made available for select fields
that belong to [SAML2 service definitions](../services/SAML2-Service-Management.html).
  
### WebAuthn FIDO Multifactor Authentication

Following on work done in previous release candidates, this release upgrades the YubiKey WebAuthn FIDO implementation to version `2.0.0`.
While this is a major upgrade internally, its exteral effects should remain largely invisible to the end-user. 
 
### CAS Proxy Authentication
        
The [proxy authentication policy](../services/Configuring-Service-Proxy-Policy.html) that may 
be assigned to a CAS service definition can now be defined using a REST endpoint. A few smaller enhancements
are also applied to the proxy policies that operate based on regular expressions with additional logging in place to assist
with better troubleshooting.
        
### Deprecated Modules

The following modules are deprecated and scheduled to be removed in future CAS versions:

- [Digest Authentication](../authentication/Digest-Authentication.html)
- [Apache Fortress Authentication](../authentication/Configuring-Fortress-Authentication.html)

## Other Stuff

- The codebase for the WebAuthn helper library is now merged into CAS as part of its core [WebAuthn feature](../mfa/FIDO2-WebAuthn-Authentication.html).
- Minor improvements to CAS documentation to display and advertise [configuration feature toggles](../configuration/Configuration-Feature-Toggles.html) better
  and automatically.
- Triggering multifactor authentication [based on a Groovy script](../mfa/Configuring-Multifactor-Authentication-Triggers-Groovy.html) is now able to support
  provider selection menus.
- Minor bug fixes to allow [Duo Security](../mfa/DuoSecurity-Authentication.html) to correctly recognize bypass rules that are defined based on principal 

## Library Upgrades

- Pac4j
- Ldaptive
- Spring Security
- MongoDb Driver
- Lombok
- Groovy
- CosmosDb
- Spring Data
- Spring Integration
- Spring Kafka
- Spring Session
- Spring Boot
- Spring Cloud
- Infinispan
- Mockito
- Micrometer
- Apache Ignite
- Micrometer
- MySQL Driver
- Material Web Components
