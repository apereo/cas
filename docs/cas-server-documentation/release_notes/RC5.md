---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.2.0-RC5 Release Notes

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

The JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. All compatible distributions
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
and scenarios. At the moment, the total number of jobs stands at approximately `511` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.
 
### Simple Multifactor Authentication

[Simple Multifactor Authentication](../mfa/Simple-Multifactor-Authentication-Registration.html) now 
allows users to register their email address when no contact information can be found for the user.
  
### Apache Tomcat 11
                                                                                        
CAS is now able to build and run on Apache Tomcat `11` to some extent. This capability is for the moment experimental 
and underlying libraries, mainly Spring Boot, do not *officially* support Tomcat `11` yet. We anticipate that this might get 
worked out and more thoroughly tested in the next CAS feature releases. You are invited to experiment and share results. 

Please remember that the baseline requirement will remain unchanged and is based on Apache Tomcat `10.1.x`. This is just a preparatory step 
to ensure CAS is ready for the next version of Apache Tomcat and Spring Boot.
  
### SCIM Attribute Resolution
         
The attribute resolution machinery in CAS is now able to [contact SCIM servers](../integration/Attribute-Resolution-SCIM.html) 
to fetch and retrieve attributes for users. [SCIM provisioning](../integration/SCIM-Provisioning.html) is also improved to allow better flexibility when it comes to 
mapping CAS attributes to the SCIM user schema.
 
### Token Authentication

[JWT authentication](../authentication/JWT-Authentication.html) is now able to accept OpenID Connect access tokens as JWTs.
  
### Google Authenticator Device Registration
    
[Google authenticator device registration](../mfa/GoogleAuthenticator-Authentication.html) can be turned off and disabled 
via CAS properties. Please note that the default behavior is kept as is, where device registration is enabled by default.
      
### Microsoft Entra ID Email Integration

CAS is now able to [send emails](../notifications/Sending-Email-Configuration-Azure-AD.html) using Microsoft Entra ID 
and its XOAUTH2 authentication mechanism.

## Other Stuff

- NameID generation for SAML2 logout requests now uses the `usernameAttributeProvider` construct assigned to the SAML2 service definition.
- [WebAuthN FIDO2 authentication](../mfa/FIDO2-WebAuthn-Authentication.html) has removed the requirement for a username from the CAS flow.
- Static resources are now resolved using a content hashing strategy that allows cache busting for static resources to avoid browser caching issues.
- [Attribute definitions](../integration/Attribute-Definitions.html) can now produce hashed values based on an assigned hashing strategy. 
- A [new actuator endpoint](../integration/Attribute-Definitions.html) allows one to fetch registered attribute definitions with CAS.
- A [new actuator endpoint](../integration/Attribute-Resolution.html) allows one to fetch registered attribute repositories with CAS.
- [X509 connector](../authentication/X509-Authentication-WebServer-Configuration.html) for Apache Tomcat configures the maximum form post and header sizes for the connector.
- CAS `<input/>` HTML elements that generate buttons are now able to render HTML content for the button label.
- Locating [SAML2 identity provider metadata](../installation/Configuring-SAML2-DynamicMetadata.html) and keys per application can now be derived from a directory location assigned to the SAML2 service definition.
- Dependency management for the CAS codebase has switched over to using Gradle's [Version Catalog](https://docs.gradle.org/current/userguide/version_catalogs.html).

## Library Upgrades

- Spring Boot
- Apache Tomcat                                        
- Spring
- BouncyCastle
- PostgreSQL
- Grouper
- Spring Kafka
- Spring Rabbit
- Sentry
- MySQL Driver
- Gradle
- Lettuce
- LettuceMod
- Groovy
- Pac4j
- Google Cloud
- SQL Server JDBC Driver
- Oracle JDBC Driver
- Guava
- Azure Maps
