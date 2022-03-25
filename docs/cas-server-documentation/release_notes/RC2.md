---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC2 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS releases are *strictly* time-based
releases; they are not scheduled or based on specific benchmarks, statistics or completion of features. To gain confidence in a particular
release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support,
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs and
operates. If you consider your CAS deployment to be a critical part of the identity and access management ecosystem, this is a viable option to consider.

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
cas.version=6.6.0-RC2
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
 
### OpenID Connect Compliance

The collection of algorithms specified in the CAS configuration for signing and encryption operations of ID tokens are now taken into account when CAS responses are produced for ID token and user profile requests. Furthermore, settings and values declared in CAS configuration for OpenID Connect discovery are now taken into account when responding or validating requests. These include supported scopes when building attribute release policies for each OpenID Connect scope, supported ACR values, response modes, response types and grant types.

### SAML2 Integration Tests

SAML2 integration tests managed by [Puppeteer](../developer/Test-Process.html) have switched to using simpleSAMLphp Docker containers for easier management and maintenance.

### OpenID Connect Issuer Aliases

CAS configuration for [OpenID Connect](../protocol/OIDC-Protocol.html) is now extended to support issuer aliases. Essentially, endpoint validation for OpenID Connect can now be be reached via alternative URLs that are trusted and registered in CAS as aliases of the issuer.

### Bucket4j Capacity Planning

Integrations with Bucket4j such as those that [throttle authentication attempts](../authentication/Configuring-Authentication-Throttling.html) or request [simple multifactor authentication](../mfa/Simple-Multifactor-Authentication.html) tokens are now able to construct and allocate buckets for individual requests as opposed to preparing a global bucket for the entire server instance. The allocation strategy is specific to the client IP address.

### OpenID Connect Client Registration

[Dynamic Client Registration](../authentication/OIDC-Authentication-Clients.html) is now able to support an expiration date for client secrets and registration requests. Authentication requests from clients with an expired client secret blocked until the application renews its client secret. Furthermore, the client configuration endpoint is now able to accept `PATCH` requests to update existing application records, or it may also be used to renew the client secret, if and when expired.

Also in a situation where the CAS is supporting open Client registration, it will now check to see if the `logo_uri` and `policy_uri` have the same host as the hosts defined in the array of `redirect_uris`.
 
### Delegation Redirection Strategy

The Groovy [redirection strategy](../integration/Delegate-Authentication-Redirection.html) for delegated authentication
is now modified to receive a list of all available providers upfront for better performance, in case the script needs to handle repeated tasks. 

<div class="alert alert-warning"><strong>Breaking Change!</strong><p>You will need to examine the script you have today and rewrite certain parts of it to handle the signature change.</p></div>

### Puppeteer Testing Strategy

The collection of end-to-end browser tests based on Puppeteer are now split into separate categories to allow the GitHub Actions job matrix to support more than `256` jobs. At the moment, total number of jobs stands at approximately `263` distinct scenarios. Furthermore, the GitHub Actions builds are now modified and improved to support running Puppeteer-based tests on Windows and MacOS.

## Other Stuff
      
- Minor UI improvements to ensure "Reveal Password" buttons line up correctly in input fields.
- The SAML2 attribute definition catalog is extended to support a few *known* attributes such as `title`, `eduPersonNickname`, etc.
- Using "Provider Selection" in combination of a multifactor authentication policy for a service that triggers on principal attributes is now supported.
- Links displayed as part of an [interrupt notification](../webflow/Webflow-Customization-Interrupt.html) can now take advantage of single sign-on sessions.
- Support for [Apache Shiro](../authentication/Shiro-Authentication.html) is now deprecated; this feature is scheduled to be removed.

## Library Upgrades
      
- Infinispan
- Netty
- TextMagic
- Apache Tomcat
- Nimbus
- Hibernate
- Spring Data
- Spring
- Spring Boot
- Spring WS
- Spring Kafka
- Spring Integration
- Hazelcast
- InfluxDb
- Micrometer
- Oshi
- Okta
- Jose4j
- Lettuce
- Apache Shiro
- Joda Time
- Font Awesome
