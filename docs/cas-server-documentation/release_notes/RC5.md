---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC5 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set 
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS 
releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks, statistics or completion of features. To gain 
confidence in a particular release, it is strongly recommended that you start early by experimenting with 
release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we 
invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership) 
and financially support the project at a capacity that best suits your deployment. Note that all development activity 
is performed *almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better 
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support, 
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will 
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs 
and operates. If you consider your CAS deployment to be a critical part of the identity and access 
management ecosystem, this is a viable option to consider.

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
cas.version=6.4.0-RC5
```

Alternatively and for new deployments, [CAS Initializr](../installation/WAR-Overlay-Initializr.html) has been updated and can also be used
to generate an overlay project template for this release.

<div class="alert alert-info">
  <strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
           
### Spring Boot 2.5

CAS is now based on the Spring Boot `2.5.x` series which by extension also requires CAS to 
upgrade its dependency on related projects such as Spring and Spring Cloud frameworks. While this 
is a significant framework upgrade, the change should remain largely invisible to CAS users and adopters.

### SAML2 Identity Provider

CAS acting as a [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) is now able to provide basic support
for browser's local storage as a means to track authentication requests. Furthermore, the handling of SAML2 authentication requests is 
now slightly modified to recognize the presence of single sign-on sessions to produce a SAML2 response directly off of the existing
session without having to redirect to CAS itself for a ticket.
                                               
### Thymeleaf User Interface Pages

The collection of thymeleaf user interface template pages are no longer found in the context root of the web application resources.
Instead, they are organized and grouped into logical folders for each feature category. For example, the pages that deal with 
login or logout functionality can now be found inside `login` or `logout` directories. The page names themselves remain unchecked.
You should always cross-check the template locations with the [CAS WAR Overlay](../installation/WAR-Overlay-Installation.html) and 
use the tooling provided by the build to locate or fetch the templates from the CAS web application context.

### CAS Documentation

A number of improvements are now implemented for a better user experience while browsing the CAS documentation:

- Pagination is now available for listed CAS properties.
- Additional details on properties that support the [Expression Language](../configuration/Configuration-Spring-Expressions.html) 
  are now displayed as tooltips.
- The setting owner is now listed, where available, for each listed property.
- Settings that control the behavior of a CAS user interface theme are now automatically documented.
- Supported [registered service properties](../services/Configuring-Service-Custom-Properties.html) are
  now automatically included in the documentation. Relevant properties are also filtered 
  by group and listed for each appropriate delegated identity provider.

## Other Stuff
       
- [SSO Sessions endpoint](../authentication/Configuring-SSO.html) now indicates the expiration 
  policy and remember-me flags for authenticated sessions.
- [Password management APIs](../password_management/Password-Management.html) are updated to allow updating security questions.
- SAML2 logout requests and responses should now respect the appropriate binding defined in CAS configuration.
- Minor adjustments to SSO participation strategies to handle `UNDEFINED` states better.
- [SAML2 authentication](../authentication/Configuring-SAML2-Authentication.html) statements 
  can now customize the `SubjectLocality` field on a per-application basis.
- HTTP request entities used for `POST` methods in REST API interactions are set to use the `UTF-8` encoding.
- Generating access tokens for the [OAuth authorization grants](../authentication/OAuth-Authentication.html) 
  is set to use client id first and then redirect URIs to locate the service definition.
- Settings and properties controlled by CAS themes are now automatically documented.
- [X509 Authentication](../authentication/X509-Authentication.html) can now handle account status errors via webflow.
- [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) repository implementations 
  are allowed to remove records by id. To handle this change, the [JPA data structures](../mfa/GoogleAuthenticator-Authentication-Registration-JPA.html)
  that keep track of scratch codes have been altered to link devices and scratch codes by id, rather than by username.
- Minor fixes to impersonation allow the surrogate user to be correctly authorized even if attribute repositories produce no attributes.
- Additional [Puppeteer tests](../developer/Test-Process.html) to cover scenarios for 
  password management, delegated authentication and OAuth protocol.
- [Groovy Interrupts](../webflow/Webflow-Customization-Interrupt-Groovy.html) are now correctly 
  initialized to recognize the script location.

## Library Upgrades

- Spring
- Spring Boot
- MariaDb Driver
- Mockito
- Amazon SDK
- OpenSAML
- Hibernate
- Micrometer
- Pac4j
- Apache Tomcat
- Spring Security
- DropWizard
- SnakeYAML
- Apache CXF
- BounctCastle
