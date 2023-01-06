---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC4 Release Notes

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

### SAML2 Delegated Authentication Metadata

SAML2 service provider metadata used and managed during [delegated authentication](../integration/Delegate-Authentication-SAML.html)
can now be stored in relational databases.

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `383` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Authentication Geolocation via Maxmind

Geolocating authentication requests via Maxmind can now support [Maxmind Web Services](../authentication/GeoTracking-Authentication-Requests.html).
 
### Lazy Initialization
 
Application components that are bootstrapped by CAS or other third-party libraries are now created lazily by default. In a nutshell, this means
that application components (and all other components and turtles that depend on those) will only be created once and when they are needed. This means
that lazy initialization may reduce the number of beans created when CAS is starting up which in turn improves the startup time. Initial test results
show that startup improvements are anywhere between `8` to `10` seconds, depending on the features and modules included in the final CAS build. At the same 
time, certain issues might be *masked* and may only be revealed at runtime since component creation is deferred. HTTP requests may also
see a small *initial* delay as the responsible component is created on-demand (but only once; this is important) to respond to the request. To accomodate 
specific use cases, certain components in CAS are also explicitly marked to always be created eagerly and skip laziness. 

If you encounter component initialization issues, deadlocks and long wait-times during CAS startup, or if you notice that background jobs, threads, event 
listeners or cleaners are not doing their job, you may of course disable this behavior via the following setting and revert back to the 
previous behavior:

```properties
spring.main.lazy-initialization=false
```

## Other Stuff

- The session cookie (typically and by default named `DISSESSION`) used for distributed session management can now be signed and encrypted in 
  scenarios where CAS is acting as an OAUTH or OpenID Connect provider, or is delegating authentication to an external identity provider.
- Configuration settings in the CAS documentation are now able to automatically indicate whether they support regular expression patterns as their value.
- Integration with [SmsMode](../notifications/SMS-Messaging-Configuration-SmsMode.html) is now upgraded to use their most recent APIs. 
- Small enhancements to distributed session management to ensure orphan `DISSESSION` cookies are not generated when locating session identifiers.

## Library Upgrades

- Groovy
- Pac4j
- Nimbus
- jQuery
- Amazon SDK
- Spring
- Spring Boot
- Mockito
- Kryo
- ErrorProne
- Checkstyle
- JavaParser
