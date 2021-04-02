---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC3 Release Notes

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
cas.version=6.4.0-RC3
```

<div class="alert alert-info">
  <strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

## CAS Initializr

[CAS Initializr](../installation/WAR-Overlay-Initializr.html) is now moved to its own separate repository. Furthermore,
the service is now able to generate overlay project templates for different CAS versions per request.

<div class="alert alert-info">
<strong>Note</strong><br/>It is expected that at some point in the not-too-distant future, previous/existing
WAR overlay projects would be deprecated and ultimately archived, allowing the CAS Initializr
to be the one true way to generate a starting template project for all CAS deployments.
</div>

## Okta Attribute Resolution

CAS attribute resolution engine now allows for fetching [user attributes from Okta](../integration/Attribute-Resolution-Okta.html).
 
## Gradle 7

The CAS codebase and the continuous integration workflows by extension have now switched to Gradle 7 for internal builds and validation.

## JDK 16 Compatibility

CAS is able to build and run successfully against the latest versions of JDK 16. The JDK baseline 
requirement continues to be set at JDK 11, and this release is keeping up with JDK releases to ensure 
CAS can correctly switch the baseline requirement when deemed necessary.

## Other Stuff
       
- Locale selection can now accept and recognize the `Accept-Language` header and user's native browser locale.  
- SAML2 registered services that define a `whiteListBlackListPrecedence` setting are now required to use `INCLUDE` or `EXCLUDE` as the accepted value.
- SAML2 metadata cache for MDQ is modified to correctly calculate the cache key for entity requests.
- Ordering and sorting of the attribute repositories is now restored to respect the `order` setting.
- Thymeleaf views specified via template prefixes in the configuration can now support `classpath` resources.
- SAML2 metadata cache can determine its expiration policy using [service expiration policy](../services/Configuring-Service-Expiration-Policy.html) if defined.
- User interface forms that contain a `username` field are set to prevent spell check and auto capitalization.
- [X509 EDIPI](../authentication/X509-Authentication.html) can now be extracted as an attribute, when available.
- [Syncope authentication](../authentication/Syncope-Authentication.html) adds support for multiple relationships of the same type.
- User interfaces fixes for login sizing related to flexbox in IE11 where the login page is far too thin to be usable.
- [Surrogate authentication](../authentication/Surrogate-Authentication.html) can correctly identify the primary principal's attributes for MFA activation.
- [SAML2 registered services](../authentication/Configuring-SAML2-Authentication.html) are correctly located from the authentication request and are matched against service provider's entity id.
- Person directory principal resolution can use attributes from the *current authentication attempt* to build the final principal.
- The ability to retry failing tests is removed from continuous integration builds to prevent test coverage miscalculations.
- Triggering continuous integration jobs and workflow runs is no longer automatically triggered to help reduce the load on the backlog.
- CAS settings able to [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) now advertise this capability in the documentation. 
- Small improvements to CI test execution to ensure coverage results can be correctly calculated.
- DynamoDb integrations can now specify the `billing-mode` in the CAS configuration.
- Triggering [multifactor authentication via principal attributes](../mfa/Configuring-Multifactor-Authentication-Triggers-Global-PrincipalAttribute.html) can now be configured to deny/block authentication attempts if no match is produced.
- [Cookie session pinning](../authentication/Configuring-SSO.html) can now allow for a set of authorized and known IP addresses to bypass failures in case mismatches are found.
- Multifactor authentication with [Duo Security](../mfa/DuoSecurity-Authentication.html) can now be tuned to turn off account status checking.

## Library Upgrades

- TestContainers
- JUnit Pioneer
- Person Directory
- Spring Boot
- Spring
- Hibernate
- Apache Velocity
- Apache jClouds
- Kryo
- Pac4j
- Lombok
- ErrorProne
- Okta
- Gradle
- OpenSAML  
- Hazelcast
- Infinispan
