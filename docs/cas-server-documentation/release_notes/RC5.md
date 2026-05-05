---
layout: default
title: CAS - Release Notes
category: Planning
---

{% include variables.html %}

# 8.0.0-RC5 Release Notes

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

The JDK baseline requirement for this CAS release is and **MUST** be JDK `25`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

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
and scenarios. At the moment, total number of jobs stands at approximately `548` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### JSpecify & NullAway

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### Spring Boot 4.1

CAS is now built with Spring Boot `4.1.x`. This is a major platform upgrade that affects almost all aspects of the codebase
including many of the third-party core libraries used by CAS as well as some CAS functionality.

### Gradle 9.6

CAS is now built with Gradle `9.6.x` and the build process has been updated to use the latest Gradle
features and capabilities.
 
### OAuth / OpenID Connect Scope Approval

OAuth and OpenID Connect have for long presented a feature where the user is required to approve scopes requested 
by the client application. This release introduces a few changes to better remember user decisions:

- Scope approval decisions are now remembered per user and client application.
- The storage mechanism is the client browser's `IndexedDB`. The user is only prompted again if there is a change in the scopes requested by the client application or if the user clears their browser data.
- There is no server-side storage of scope approval decisions and there is nothing sensitive in the data that is stored by the browser.

<div class="alert alert-info">:information_source: <strong>Note</strong>
<p>We are not talking about Attribute Consent; a standalone separate feature. This is specifically
about the approval screen that lists all OAuth or OpenID Connect scopes requested by the relying party.
</p></div>

`IndexedDB` is supported by current versions of Chrome, Edge, Firefox, Safari, iOS Safari, and most modern mobile browsers. 
Can I Use lists it as widely supported across modern browsers, with older versions showing partial or missing support. However, 
very old browsers may not support `IndexedDB` or may support older/buggy versions. This matters most for old Android WebViews, 
old iOS WebViews, old Safari, and legacy enterprise browsers.

CAS tries to detect if `IndexedDB` is supported by the browser and will react accordingly. That said, if this prove problematic, 
you can always disable scope approval requests either globally or per client application.
          
If you have already turned off scope approval, there is nothing here for you to. Keep calm and carry on. 
 
### Impersonation
               
[Palantir](../installation/Admin-Dashboard.html) is given the ability to support 
[impersonation features](../authentication/Surrogate-Authentication.html) of CAS
and report back eligible impersonation accounts for a given username. This functionality is supported by 
the new actuator endpoint, `impersonation`, that can be used to query for eligible surrogate accounts.

## Other Stuff
              
- Minor user interface adjustments to remove unneeded resources and script references from various pages.
