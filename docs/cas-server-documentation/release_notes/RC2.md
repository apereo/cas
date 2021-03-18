---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC2 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set 
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS 
releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks, statistics or completion of features. To gain 
confidence in a particular release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership) 
and financially support the project at a capacity that best suits your deployment. Note that all development activity 
is performed *almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better 
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support, 
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will 
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs 
and operates. If you consider your CAS deployment to be a critical part of the identity and access management ecosystem, this is a viable option to consider.

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
cas.version=6.4.0-RC2
```

<div class="alert alert-info">
  <strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release. 
  
### CAS Initializr 

[CAS Initializr](../installation/WAR-Overlay-Initializr.html) is now given the ability to produce
different overlay projects using a dedicated `type` parameter.

<div class="alert alert-info">
<strong>Note</strong><br/>It is expected that at some point in the not-too-distant future, previous/existing 
WAR overlay projects would be deprecated and ultimately archived, allowing the CAS Initializr 
to be the one true way to generate a starting template project for CAS deployments.
</div>

### CAS Documentation

CAS documentation has gone through a cleanup effort to improve how configuration settings are
managed and presented. Configuration namespaces for CAS settings are presented as individual
snippets and fragments appropriate for each feature, and are included throughout the documentation
pages where necessary, split into panes for required, optional and third-party settings, etc.

The presentation and generation of CAS settings and their documentation is entirely driven by CAS configuration metadata,
and this capability is ultimately powered by Github Pages and Jekyll that render the CAS documentation in the backend.

Please note that as part of this change, a number of CAS configuration settings are moved around into new namespaces
to make the generation of configuration metadata and relevant documentation snippets easier. Most likely, settings
are moved into a new `.core.` or `.engine.` or `.policy` namespace. Some of the settings that are affected by this effort
are:

- `cas.authn.adaptive`
- `cas.service-registry`
- `cas.authn.mfa`
- `cas.authn.mfa.trusted`
- `cas.authn.saml-idp`
- `cas.attribute-repository`
- `cas.authn.pac4j`
- `cas.ticket.tgt`

<div class="alert alert-info">
<strong>Note</strong><br/>Configuration changes for the most commonly-used settings
are recorded in the CAS configuration metadata catalog to note the change and the possible replacement.
You should be receiving warnings and/or instructions on startup if your configuration is affected by
the above changes. 
</div>

## Pac4j v5

The Pac4j library, mainly responsible for delegated authentication, is now upgraded to its `v5` release line. This is a major upgrade
with many API changes that affect the internal workings of CAS when it comes to dealing with an external identity provider
or managing the internal implementation of OpenID Connect and SAML2 protocols when CAS is acting as a standalone identity provider.
Pac4j `v5` is not quite final yet, and we are taking advantage of the early release candidate here to do as much work upfront
as possible to handle the final upgrade better in the future. As a result, some things may not be immediately functional
and, as always, you are encouraged to try and test the upgrade as much as possible to avoid surprises.

## Delegated Authentication Per Application

Certain configuration elements for an external identity provider, in the context of
[delegated authentication](../integration/Delegate-Authentication.html) can now be controlled
and overridden on a per-application basis. For example, you may choose to send a specific SAML2
authentication context class to an external SAML2 identity provider depending on the original
service provider, while leaving the global configuration in place and unmodified. 
Support for service-level overrides applies to SAML2 and OIDC identity providers.

## Scriptable Email Messages

The construction of the [email message body](../notifications/Sending-Email-Configuration.html) can 
now be scripted using an external Groovy script.

## Delegated Authentication Tracking

Identity provider choices and selections made via [delegated authentication](../integration/Delegate-Authentication.html) 
can now be remembered via a dedicated cookie to be used for subsequent attempts and auto-redirects.

## Other Stuff

- A number of Docker images used for [integration tests](../developer/Test-Process.html), such as 
  DynamoDb, MySQL, MariaDb, etc are now updated to their latest versions.
- A special failure analyzer for Spring Boot is now available to analyze startup failures more accurately and with better logs.
- In [delegated authentication](../integration/Delegate-Authentication-SAML.html) to SAML2 identity providers,
  handling SAML2 logout requests and responses should now properly honor final redirects back to the calling application.
- Support for the legacy syntax for [JSON service definitions](../services/JSON-Service-Management.html) 
  based on CAS Addons as well as the old `org.jasig` namespace has been removed. 
- Locale interception and changing the default user interface language can now be forced regardless of the http request.  
- Reworking internal components and APIs for [password management](../password_management/Password-Management.html) to 
  make customizations easier, specially when multiple fields may be involved to locate the user record. The work here may present
  breaking changes, specially if you are handling password 
  management operations via external [Groovy scripts](../password_management/Password-Management-Groovy.html).
- The default value for `cas.service-registry.git.branches-to-clone` and `cas.authn.saml-idp.metadata.git.branches-to-clone`
  changed from `master` to `*` which means all branches will be cloned by default. The properties may contain a list of
  branches, but the list must include the branch specified in the `cas.service-registry.git.active-branch`
  or `cas.authn.saml-idp.metadata.git.active-branch` property. 
- When resolving the final principal to build the authentication object and history, attributes from all collected authentication
  objects are now merged back together using the merge strategy defined in CAS configuration instead of the hardcoded `MultivaluedAttributeMerger`.
- References to [Bintray repositories](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/) 
  have been removed and updated with more maintainable replacements.
- SAML2 IdP metadata generators now allow for dynamic customizations at runtime when generating metadata.
- When using [delegated authentication](../integration/Delegate-Authentication-SAML.html) to SAML2 identity providers, the service provider metadata
can now be signed using the `XMLSec` tool.
- [SAML2 Attribute Definitions](../integration/Attribute-Definitions.html) now present the ability to support `urn` values for each attribute.  
- [SAML2 IdP metadata](../installation/Configuring-SAML2-DynamicMetadata.html) can now be signed.
- Minor improvements to indexing operations for [MongoDb ticket registry](../ticketing/MongoDb-Ticket-Registry.html).
- Additional support is built in to provide for Spring Boot's `startup` actuator endpoint.
- In [delegated authentication](../integration/Delegate-Authentication.html) scenarios, CAS is now able to retry the authentication attempt
using forceful authentication requests upon failed attempts.
- Principal election strategy can now be directed to choose the correct principal from the chain, when multiple conflicting principals are resolved.
- [Remember-Me Authentication](../authentication/Configuring-LongTerm-Authentication.html) can now be 
  conditionally activated based on certain user-agents or IP addresses.  
- [Attribute consent](../integration/Attribute-Release-Consent.html) implementations are given the ability to delete consent decisions by user.  
- [Attribute release policies](../integration/Attribute-Release-Policies.html) that allow for a collection of pre-defined attributes for release
can now request those attributes to be resolved via the [Attribute Definition Store](../integration/Attribute-Definitions.html).

## Library Upgrades

- Apache Tomcat
- Nimbus OIDC
- Nimbus JWT
- Google Maps
- Couchbase Client
- MariaDb Driver
- PostgreSQL Driver
- Spring Cloud
- Spring Security
- Amazon SDK
- Spring
- JavaParser
- Pac4j
- Hibernate
- Hibernate Validator
- Commons Lang  
- Spring Boot
- Caffein
- JQuery
- ByteBuddy
- SnakeYAML
- SpotBugs
