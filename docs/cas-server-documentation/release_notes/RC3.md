---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC3 Release Notes

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
cas.version=6.5.0-RC3
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the 
minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
    
### Account (Self-Service) Registration

CAS provides a modest workflow to 
handle [self-service account registration](../registration/Account-Registration-Overview.html).
This capability was developed and first released in `6.5.0-RC1`, and it will be repeatedly refined
and improved in the future to match and accommodate realistic workflows deployed today as much as possible. 
 
### OpenID Connect Key Rotation
     
CAS can now be configured to rotate keys in the OpenID Connect keystore automatically 
based on a predefined schedule. Rotation will include previous keys as well as current and future
keys to assist with integrations and caching concerns. There is also a revocation schedule for old
inactive keys that should be removed from the keystore.

## Other Stuff

## Library Upgrades

- Spring Boot

