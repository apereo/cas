---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC9 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks,
statistics or completion of features or bug fixes. To gain confidence in a particular
release, we recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

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
- [Support](https://apereo.github.io/cas/Support.html)

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html).
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### CAS Initializr & OpenRewrite

[CAS Initializr](../installation/WAR-Overlay-Initializr.html) is now able to produce upgrade recipes based on [OpenRewrite](https://docs.openrewrite.org/).
The goal is to allow for in-place automatic upgrades using recipes that understand the differences and nuances from one CAS version to the next.

<div class="alert alert-info">:information_source: <strong>Rough Waters Ahead</strong><p>
Like a cake still baking in the oven, this feature is a masterpiece in the making. It might be still a little doughy and 
half-baked, so approach with care and a pinch of curiosity. </p></div>

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `453` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.

### Account Profile & Multifactor Authentication

[Account profile management](../registration/Account-Management-Overview.html) is now able to allow users to register their own multifactor authentication devices.
Registration flows are supported for [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) 
and [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication-Registration.html).

### Refactoring & Cleanup

There has been lots of internal cleanup, refactoring and build system improvements to remove assumptions and hard dependencies between CAS modules.
As a result, about **33 MB worth of cruft** has been removed from the CAS web application and the final packaged artifact is now leaner. While the changes
should generally prove invisible to the deployer, functionality that deals with fetching attributes from LDAP and Active Directory
systems should be reviewed carefully to make sure [the correct module listed here](../integration/Attribute-Resolution-LDAP.html) is included in the build
where and when necessary.

### OpenID Connect Access Token Authentication

Requests to the OpenID Connect access token endpoint have started to enforce the [required authentication method](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html)
assigned to the relying party and registered service definition. The default authentication method has always been `client_secret_basic`
and the endpoint will begin to enforce this for all requests. Furthermore, authentication methods will be deactivated and disabled
if support for a method is not defined and declared in OpenID discovery metadata.

### Authentication Policy & Credential Metadata

The structure of the [Groovy Authentication Policy](../authentication/Configuring-Authentication-Policy-Groovy.html) is 
now updated to allow it access to the full authentication attempt, etc. **This is a breaking change** and you will be
expected to update your script definitions to match the input requirements and parameters.

Furthermore, `CredentialMetadata` objects attached to authentication attempts may carry extra properties attached
to the credential itself. This bit is not new, of course. In scenarios where authentication attempts are made 
using the [CAS REST API](../protocol/REST-Protocol-CredentialAuthentication.html),
the extracted credential will carry properties that are populated with request headers, etc.
 
### Access Strategy and Unauthorized Redirect URLs

Access strategy configuration blocks assigned to service definitions have the ability to assign a URL to the definition
to which [CAS would redirect](../services/Service-Access-Strategy-URL.html) when authorization and access is denied.
While redirect has always been somewhat immediate and invisible, CAS should now inform the user that the redirect is
taking place after a small delay window, `2~3` seconds, and the final redirect is then made via the browser. 

### OpenID Connect Identity Assurance

Modest enhancements are in place to support [OpenID Connect Identity Assurance](https://openid.net/specs/openid-connect-4-identity-assurance-1_0-12.html).
Efforts in this area mainly include:

- Auditing the generation of OpenID Connect ID tokens
- Generating a `txn` claim for audit payloads to tie the record to the ID token transaction
- ID token audit payload may include the authentication methods employed

Furthermore, [built-in support](../authentication/OIDC-Authentication-Identity-Assurance.html) is now available 
for a special `assurance` scope that carries special additional claims
about end-users such as `place_of_birth`, `salutation`, `birth_given_name`, etc.

### Ticket Tracking, OAuth & OpenID Connect

A ticket-granting ticket often carries a number of child or descendent tickets for whom it acts as the parent entity.
These child tickets are often those that are backed and supported by alternative protocols, such as OAuth access tokens, 
refresh tokens, etc. Typically and by default, when a parent ticket-granting ticket is removed from CAS either explicitly
or during logout operations, all such descendent tickets are not removed. Depending on the use case, this may be
desirable specially in scenarios where you wish for these descendent tickets such as OAuth refresh tokens
to last a super long time well beyond the lifetime of the ticket-granting ticket that sponsored their existence. Starting
in this release candidate, CAS begins to provide options to allow the deployer to control the ticket tracking mechanism, 
to determine whether such child tickets should be tracked. The decision in favor of tracking such tickets will of course 
also remove them from the registry during logout operations, etc.

## Other Stuff

- When using [OpenID Connect](../protocol/OIDC-Protocol.html), requests that carry a `client_secret` as a query parameter are now rejected and/or ignored.
- Changes to Spring Security configuration to allow basic authentication requests to pass through correctly to CAS endpoints where appropriate.
- Apache Shiro, only used to assist with database password encoding and hashing functions, is now fully removed from CAS codebase.
- A new and somewhat humble [actuator endpoint](../ticketing/Configuring-Ticketing-Components.html) to interact with and query the CAS ticket registry.
- X.509 client authentication is now available as an authentication for the access token endpoint in [OpenID Connect](../protocol/OIDC-Protocol.html). 
- Authentication requests received from the [Shibboleth IdP](../integration/Shibboleth.html) are able to locate the correct service definition in the registry. 
- [Apache Tomcat](../installation/Servlet-Container-Embedded-Tomcat-RemoteUserValve.html) is now able to support `REMOTE_USER` authentication directly.
- Internal refactoring of the [Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html) to support custom key construction and patterns for ticket objects and other entries.
- Incremental improvements to [authentication webflow interrupts](../webflow/Webflow-Customization-Interrupt.html) to handle interrupt tracking better when payloads change dynamically.
- Client TLS settings are now supported for [OpenID Connect](../authentication/OIDC-Authentication-AccessToken-AuthMethods.html) relying parties and applications. This capability is also extended to [Dynamic Client Registration](../authentication/OIDC-Authentication-Dynamic-Registration.html) requests.
- CAS notifications can now be [sent to Slack](../notifications/Notifications-Configuration-Slack.html) too.
- The header value for `Strict-Transport-Security` header [can now be defined](../services/Configuring-Service-Http-Security-Headers.html) both globally and per application.
- Groovy scripts that determine user interface theme names may now be cached for subsequent executions.

## Library Upgrades
  
- Jacoco 
- Jackson
- Spring
- Spring Boot
- Spring Data
- Spring Security
- Spring Integration
- Spring Kafka
- Spring AMQP
- Apache Log4j
- Hjson
- Apache Tomcat
- Pac4j
- Hazelcast
- JAXB
- Twilio
- Amazon SDK
- Commons CLI
- Commons IO
- Swagger
- jsoup
- HikariCP
- CosmosDB
- JUnit
- SendGrid
- Netty
- Nimbus OIDC
- Spring Shell
- MariaDb
- MongoDb Driver
- Azure Spring Cloud
- MySQL Driver
- MSSQL Driver
- Micrometer
