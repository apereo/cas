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

The Pac4j library, mainly responsible for delegated authentication, is now upgraded to `v5`. This is a major upgrade
with many API changes that affect the internal workings of CAS when it comes to dealing with an external identity provider
or managing the internal implementation of OpenID Connect and SAML2 protocols when CAS is acting as a standalone identity provider.
Pac4j `v5` is not quite final yet, and we are taking advantage of the early release candidate here to do as much work upfront
as possible to handle the final upgrade better in the future. As a result, some things may not be immediately functional
and, as always, you are encouraged to try and test the upgrade as much as possible to avoid surprises.

## Scriptable Email Messages

The construction of the [email message body](../notifications/Sending-Email-Configuration.html) can 
now be scripted using an external Groovy script.

## Other Stuff
      
- A number of Docker images used for [integration tests](../developer/Test-Process.html), such as DynamoDb, MySQL, MariaDb, etc are now updated to their latest versions.
- A special failure analyzer for Spring Boot is now available to analyze startup failures more accurately and with better logs.
- Support for the legacy syntax for [JSON service definitions](../services/JSON-Service-Management.html) based on CAS Addons as well as the old `org.jasig` namespace has been removed. 
- Reworking internal components and APIs for [password management](../password_management/Password-Management.html) to make customizations easier, specially when multiple fields may be involved to locate the user record.

## Library Upgrades

- Apache Tomcat
- Nimbus OIDC
- Nimbus JWT
- Google Maps
- Couchbase Client
- MariaDb Driver
- Spring Cloud
- Pac4j
