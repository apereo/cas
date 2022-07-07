---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC5 Release Notes

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
cas.version=6.6.0-RC5
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Refreshable JDBC/JPA Integrations

CAS modules that offer an integration with a relational database, such as [JPA Service Registry](../services/JPA-Service-Management.html), 
are now extensively enhanced and tested to ensure the underlying components can correctly respond to *refresh* events, when 
the application context is reloaded once a property is changed.

### Refreshable Delegated Identity Providers

The collection of external identity providers that are used for [delegated authentication](../integration/Delegate-Authentication.html)
can now be [registered with CAS](../integration/Delegate-Authentication-Provider-Registration.html) using an external 
REST API that is able to produce identity provider configuration using a collection of CAS-owned properties as the final payload.

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover
more use cases and scenarios. At the moment, total number of jobs stands at approximately `312` distinct
scenarios. The overall test coverage of the CAS codebase is approximately `94%`.

## Other Stuff
     
- The configuration for the embedded Apache Tomcat is now allowed to create multiple connectors and ports. The `secure` and `scheme` attributes are also 
  customizable.
- Rules for redirecting to a service after a logout operation are now tightened to make sure protocol cross-over is not possible.
- The OpenID Connect userinfo endpoint is now able to correctly process mapped claims defined in CAS configuration.
- Account profile management is now able to allow entry access when CAS is set to disallow missing `service` parameters.
- Certain ticket-related operations are now removed from the `CentralAuthenticationService` interface, as all transaction-bound operations should now be 
  moved inside the `TicketRegistry` interface.
- Minor adjustments to attribute release policies, with particular attention to reusing resolved attributes as part of a chain.
- On successful login attempts and in case no `service` is specified, CAS will redirect to [Account profile management](..
  /registration/Account-Management-Overview.html) in case the feature is enabled.


## Library Upgrades

- Spring Security
- Google Firebase
- Spring Data
- Spring Boot
- Spring Integration
- Dropwizard
- Gradle
- Amazon SDK
- Okio
- Acme4j
- Twilio
- CosmosDb
- MariaDb
- Apache Log4j
- Apache Curator
- Spring Boot Admin
