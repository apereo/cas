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

### Multifactor Registered Devices

Devices that are registered with CAS for multifactor authentication flows and integrations can now be listed
in the [account profile dashboard](../registration/Account-Management-Overview.html) page. At the moment,
the supported multifactor providers for this capability
are [Duo Security](../mfa/DuoSecurity-Authentication.html) and [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html).

<img width="1699" alt="image" src="https://user-images.githubusercontent.com/1205228/164191147-1864c987-a339-4678-98e6-54d2beb8200c.png">

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover more use cases and scenarios. At the moment, total number of jobs 
stands at approximately `285` distinct scenarios. The overall test coverage of the CAS codebase is approximately `94%`.

### Groovy Webflow Actions

Certain Spring Webflow actions are now given the option for an [alternative Groovy implementation](../webflow/Webflow-Customization-Extensions.html). This  
allows one to completely replace the Java implementation of a Spring webflow action that is provided by CAS with a Groovy script for custom use cases and
total control in scenaios where using Java may not be ideal or possible.

## Other Stuff

- The codebase for the WebAuthn helper library is now merged into CAS as part of its core [WebAuthn feature](../mfa/FIDO2-WebAuthn-Authentication.html).
- Minor improvements to CAS documentation to display and advertise [configuration feature toggles](../configuration/Configuration-Feature-Toggles.html) better
  and automatically.
- Triggering multifactor authentication [based on a Groovy script](../mfa/Configuring-Multifactor-Authentication-Triggers-Groovy.html) is now able to support
  provider selection menus.
- Minor bug fixes to allow [Duo Security](../mfa/DuoSecurity-Authentication.html) to correct recognize bypass rules that are defined based on principal 

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
- Infinispan
- Mockito
- Micrometer
