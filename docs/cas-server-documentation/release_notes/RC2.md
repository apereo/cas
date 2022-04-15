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

The collection of algorithms specified in the CAS configuration for signing and encryption operations of ID tokens are now taken into account when CAS responses are produced for ID token and user profile requests. Furthermore, settings and values declared in CAS configuration for OpenID Connect discovery are now taken into account when responding or validating requests. These include supported scopes when building attribute release policies for each OpenID Connect scope, supported ACR values, response modes, prompt values, response types and grant types.

### SAML2 Integration Tests

SAML2 integration tests managed by [Puppeteer](../developer/Test-Process.html) have switched to using simpleSAMLphp Docker containers for easier management and maintenance.

### OpenID Connect Issuer Aliases

CAS configuration for [OpenID Connect](../protocol/OIDC-Protocol.html) is now extended to support issuer aliases. Essentially, endpoint validation for OpenID Connect can now be be reached via alternative URLs that are trusted and registered in CAS as aliases of the issuer.

### Bucket4j Capacity Planning

Integrations with Bucket4j such as those that [throttle authentication attempts](../authentication/Configuring-Authentication-Throttling.html) or request [simple multifactor authentication](../mfa/Simple-Multifactor-Authentication.html) tokens are now able to construct and allocate buckets for individual requests as opposed to preparing a global bucket for the entire server instance. The allocation strategy is specific to the client IP address.

### Feature Toggles

Support for [feature toggles](../configuration/Configuration-Management-Extensions.html) is now extended and handled by all CAS modules.

### OpenID Connect Client Registration

[Dynamic Client Registration](../authentication/OIDC-Authentication-Clients.html) is now able to support an expiration date for client secrets and registration requests. Authentication requests from clients with an expired client secret blocked until the application renews its client secret. Furthermore, the client configuration endpoint is now able to accept `PATCH` requests to update existing application records, or it may also be used to renew the client secret, if and when expired.

Also in a situation where CAS is supporting open client registration, it will now check to see if the `logo_uri` and `policy_uri` have the same host as the hosts defined in the array of `redirect_uris`.
 
### Delegation Redirection Strategy

The Groovy [redirection strategy](../integration/Delegate-Authentication-Redirection.html) for delegated authentication
is now modified to receive a list of all available providers upfront for better performance, in case the script needs to handle repeated tasks. 

<div class="alert alert-warning"><strong>Breaking Change!</strong><p>You will need to examine the script you have today and rewrite certain parts of it to handle the signature change.</p></div>

### CAS Initializr Projects

[CAS Initializr](../installation/WAR-Overlay-Initializr.html) is now updated to produce and sync WAR overlay projects for the [Spring Cloud Configuration Server](../configuration/Configuration-Server-Management-SpringCloud.html). Furthermore, along with the `6.4.x` and `6.5.x` releases of the CAS Management web application, CAS Initializr has been updated to produce WAR overlays for those builds as well.

### SAML2 Authentication Context Class

Building a SAML2 authentication context class can now be done in more dynamic 
ways using a [Groovy script](../installation/Configuring-SAML2-AuthnContextClass.html). 

### Spring Framework RCE

As part of routine dependency upgrades and library maintenance, the version of the Spring Framework used by CAS is also bumped to remove the threat of the RCE vulnerability [discussed here](https://apereo.github.io/2022/03/31/spring-vuln/).

### Puppeteer Testing Strategy

The collection of end-to-end browser tests based on Puppeteer are now split into separate categories to allow the GitHub Actions job matrix to support more than `255` jobs. At the moment, total number of jobs stands at approximately `277` distinct scenarios. Furthermore, the GitHub Actions builds are now modified and improved to support running Puppeteer-based tests on Windows and MacOS.

### Password Management

CAS may also allow individual end-users to update certain aspects of their account that relate to password management in a *mini portal* like setup, such as resetting the password or updating security questions, etc.

<img width="100%" alt="image" src="https://user-images.githubusercontent.com/1205228/160280056-ec2244f1-acb3-44fb-93cc-ee3ac5e541e6.png">

### Groovy GeoLocation

Authentication requests can be mapped and geo-tracked to [physical locations](../authentication/GeoTracking-Authentication-Requests.html) using Groovy scripts. 

### Google Authenticator Scratch Codes

CAS now allows to encrypt the Google Authenticator scratch codes to protect their values. This is enabled when the following key is set: `cas.authn.mfa.gauth.core.scratch-codes.encryption.key`. You must notice that while the encrypted scratch codes are still numbers, they are in fact encrypted forms of the same scratch code encoded as large numbers. Note that previous, existing scratch codes will continue to work as they did before.

<div class="alert alert-warning"><strong>Breaking Change!</strong><p>You may need to massage the underlying data model to account for this change. See notes below on how to handle this for relational databases.</p></div>

In case you are managing device registration records in a database, the `scratch_codes` column in the `scratch_codes` table in the database needs to be updated. For example for PostgreSQL, you must run this SQL command to alter the column from an `int4` to a `numeric`:

```sql
ALTER TABLE scratch_codes ALTER COLUMN scratch_codes TYPE numeric USING scratch_codes::numeric;
```

This should be very similar for other databases: you need to migrate the column type from `integer` to `numeric`.

## Other Stuff
      
- Minor UI improvements to ensure "Reveal Password" buttons line up correctly in input fields.
- The SAML2 attribute definition catalog is extended to support a few *known* attributes such as `title`, `eduPersonNickname`, etc.
- Using "Provider Selection" in combination with a multifactor authentication policy for a service that triggers on principal attributes is now supported.
- Links displayed as part of an [interrupt notification](../webflow/Webflow-Customization-Interrupt.html) can now take advantage of single sign-on sessions.
- Support for [Apache Shiro](../authentication/Shiro-Authentication.html) is now deprecated; this feature is scheduled to be removed.
- Minor bug fixes to correct the device registration flow for [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html). 
- Documentation improvements to take advantage of [DataTables](https://www.datatables.net/) instead to show and paginate CAS configuration properties.
- Support for graceful shutdowns for all *embedded* servlet containers such as Apache Tomcat.
- Multifactor provider selection can now function in [delegated authentication](../integration/Delegate-Authentication.html) flows when required.
- OAuth and OpenID Connect userinfo/profile endpoints are now able to accept `application/jwt` as a supported content type.

## Library Upgrades
      
- Infinispan
- Netty
- TextMagic
- Apache Tomcat
- Nimbus
- OpenSAML
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
- Hibernate
- MariaDb
- Oshi
- Okta
- Jose4j
- Lettuce
- Apache Shiro
- Joda Time
- Font Awesome
- Pac4j
