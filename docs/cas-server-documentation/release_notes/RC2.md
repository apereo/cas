---
layout: default
title: CAS - Release Notes
category: Planning
---

{% include variables.html %}

# 8.0.0-RC2 Release Notes

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
and scenarios. At the moment, total number of jobs stands at approximately `535` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. 
                                                                                                                            
### Session Replication (Deprecated)

Crypto operations that support generating cookies for *deprecated session replication capabilities* are now turned off by default
to avoid generating keys. Furthermore, the session replication capabilities that are not based on the
[ticket registry](../webflow/Webflow-Customization-Sessions-ServerSide-TicketRegistry.html) are now turned off 
by default. If you are using the deprecated legacy session replication features, you MUST explicitly 
turn on the following settings:

```properties
cas.x.y.z.session-replication.replicate-sessions=true
cas.x.y.z.session-replication.cookie.crypto.enabled=true
```

Remember to substitute `x.y.z` with the appropriate session replication 
module version you are using (i.e. `oauth`, `saml-idp`, etc):
                                                             
```properties
cas.authn.pac4j.core.session-replication.replicate-sessions=true
cas.authn.pac4j.core.session-replication.cookie.crypto.enabled=true
          
cas.authn.oauth.session-replication.replicate-sessions=true
cas.authn.oauth.session-replication.cookie.crypto.enabled=true
                
cas.authn.saml-idp.core.session-replication.replicate-sessions=true
cas.authn.saml-idp.core.session-replication.cookie.crypto.enabled=true
```

### Palantir Admin Dashboard

[Palantir Admin Console](../installation/Admin-Dashboard.html) received significant changes across the board
to handle and support more actuator endpoints, when it comes to adding external identity providers, retrieving
user sessions, listing multifactor authentication providers, etc.

### Gradle 9.4

CAS is now built with Gradle `9.4` and the build process has been updated to use the latest Gradle
features and capabilities. This also prepares future CAS versions to build and run against JDK `26`.

### Spring Boot 4

CAS is now built with Spring Boot `4.1.x`. This is a major platform upgrade that affects almost all aspects of the codebase
including many of the third-party core libraries used by CAS as well as some CAS functionality.
     
#### Spring Cloud Bus w/ AMQP

Support for [Spring Cloud Bus with AMQP](../configuration/Configuration-Management-Clustered-AMQP.html) is 
not yet quite compatible with Spring Boot `4.1.x`. We will reintroduce this support in a future release 
once compatibility is restored.

### JSpecify & NullAway

CAS codebase is now annotated with [JSpecify](https://jspecify.dev/) annotations to indicate nullness contracts on method parameters,
return types and fields. We will gradually extend the coverage of such annotations across the entire codebase in future releases
and will integrate the Gradle build tool with tools such as [NullAway](https://github.com/uber/NullAway) to prevent nullness contract violations
during compile time.

### SpringBoot Admin

Support for [SpringBoot Admin](../monitoring/Configuring-SpringBootAdmin.html) is now compatible with Spring Boot `4.x`.

## Other Stuff
  
- Compiled valid regular expressions are now cached to improve performance across the board.
- Continued efforts using advanced code analysis techniques to remove potential memory leaks and improve system performance.
- CAS is now upgraded to use `jQuery` version `4.0.0`.
- Integration tests have switched to use MySQL `9.6.x`.
- Better log data sanitization to avoid logging sensitive information, particularly when `DEBUG` logs are enabled.
- Minor bug fixes to improve [OpenID Token Exchange](../authentication/OAuth-ProtocolFlow-TokenExchange.html).
- Sorting operations and locating service definitions registered with CAS is now optimized to improve performance large numbers of applications.
