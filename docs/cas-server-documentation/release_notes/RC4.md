---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC4 Release Notes

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
cas.version=6.6.0-RC4
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the 
minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
 
### Active Single Sign-on Sessions

The following ticket registry implementations are enhanced to provide dedicated native queries
and support for fetching active single sign-on sessions for a given username.
                                                                                
- [MongoDb Ticket Registry](../ticketing/MongoDb-Ticket-Registry.html)
- [Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html)
- [JPA Ticket Registry](../ticketing/JPA-Ticket-Registry.html)
- [DynamoDb Ticket Registry](../ticketing/DynamoDb-Ticket-Registry.html)
- [Hazelcast Ticket Registry](../ticketing/Hazelcast-Ticket-Registry.html) using [Hazelcast Jet](https://jet-start.sh/)

<div class="alert alert-info">
<strong>Note</strong><br/>Remember that all other registry implemntations support this capability.
However, their execution and performance may not be as ideal as those that support dedicated queries.
</div>

Dedicated queries should lead to better performance, though the results may not be immediately obvious
specially if/when the ticket registry is configured to sign and encrypt tickets.

### Deprecated Modules

The following modules are deprecated and scheduled to be removed in future CAS versions:

- [Infinispan Ticket Registry](../ticketing/Infinispan-Ticket-Registry.html)
- [Couchbase Ticket Registry](../ticketing/Couchbase-Ticket-Registry.html)

## Other Stuff
            
- The token introspection for [OAuth](../protocol/OAuth-Protocol.html) and [OpenID Connect](../protocol/OIDC-Protocol.html) is now fixed to show the correct 
  response for invalid/unknown tokens.

## Library Upgrades

- InfluxDb
- Amazon SDK
- ErrorProne
- Gradle
- MariaDb Driver
- PostgreSQL Driver
