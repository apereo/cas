---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC4 Release Notes

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
cas.version=6.5.0-RC4
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the 
minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Account Registration SCIM Provisioning

Account registration requests may be [provisioned via SCIM](../registration/Account-Registration-Provisioning-SCIM.html).

### reCAPTCHA Activation Strategy
  
Integrations with [reCAPTCHA](../integration/Configuring-Google-reCAPTCHA.html) are now able to customize
the activation strategy to determine reCAPTCHA initialization and validation behavior, globally and/or 
on a per-service basis.

### Attribute Consent via DynamoDb

Attribute consent decisions and records can now be managed and 
stored via [DynamoDb](../integration/Attribute-Release-Consent-Storage-DynamoDb.html).

### Delegated Authentication SCIM Provisioning

Delegated authentication to external identity providers is now able to provision established profiles
and identities via [SCIM](../integration/Delegate-Authentication-Provisioning.html).
  
### Signing SAML2 Assertions Per Service

CAS is now able to provide more granular control over how SAML2 assertions in the final response are signed. The
`signAssertions` flag in the CAS service definition for the SAML service provider 
can now accept a *tri-state* boolean with the following values:

- `TRUE`: Force sign the assertion.
- `FALSE`: Never sign the assertion.
- `UNDEFINED`: Evaluate the signing requirement based on the `WantAssertionsSigned` flag found in the service provider metadata.

<div class="alert alert-warning"><strong>Breaking Change</strong><p>
This <i>might</i> be a breaking change. Please examine your service definitions to review this setting. While 
the default being <code>false</code> has not changed, it might result in a change in behavior 
compared to previous releases and updates.
</p></div>

Previously-recorded boolean values for this flag as `true` or `false` should be 
automatically translated by CAS to their `TRUE` or `FALSE` equivalent. 
  
### Ticket Registry Distributed Locks

Ticket registry operations should now support [distributed locking](../ticketing/Ticket-Registry-Locking.html).
Lock implementations are available for JVM memory (default), Redis, ZooKeeper and JDBC technologies.

### Hazelcast Discovery w/ Google Cloud Platform

[Hazelcast Ticket Registry](../ticketing/Hazelcast-Ticket-Registry.html) member discovery is now extended to support [Google Cloud Platform](../ticketing/Hazelcast-Ticket-Registry-AutoDiscovery-Docker-GCP.html) deployments.

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow 
to add additional scenarios. At this point, there are
approximately `220` test scenarios and we'll continue to add more in the coming releases.

## Other Stuff
                   
- The schedulers responsible for OpenID Connect key rotation and revocation are disabled by default to prevent changes to the keystore on startup.
- Improvements to CAS ticket registry operations to handle concurrent ticket and token requests correctly.     
- External integration tests are now available for [SCIM](../integration/SCIM-Integration.html).
- Several CAS components internally are now marked with `@RefreshScope` to participate in application context refresh attempts.
- SAML2 metadata generation can populate the `errorURL` attribute with an `idp/error` public endpoint to show a generic error page.
- Performance improvements to all Redis integrations so `SCAN` operations work correctly and do not exhaust the system.

## Library Upgrades

- Spring Cloud
- SpotBugs
- Hibernate
- Apache Tomcat
- Thymeleaf
- Pac4j
- Grouper Client
- Micrometer
- Dropwizard
- Log4j2
- Kryo
- Java Melody
- Splunk
- Logback
- Spring WS
- Oshi
- DropWizard
- SnakeYAML
- Bucket4j
- Okta SDK
- Spring
- Spring Boot
- Spring Security
- Hazelcast
