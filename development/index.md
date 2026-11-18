# Home

Source: https://apereo.github.io/cas/development/index.html

THE OPEN SOURCE IDENTITY PLATFORM

# Apereo CAS - Enterprise Authentication For All Earthlings and Beyond

Welcome to the home of the Apereo Central Authentication Service project, more commonly referred to as CAS. CAS is an enterprise multilingual single sign-on solution and identity provider for the web and attempts to be a comprehensive platform for your authentication and authorization needs.

CAS is an open and well-documented authentication protocol. The primary implementation of the protocol is an open-source Java server component by the same name hosted here, with support for a plethora of additional authentication protocols and features.

[New to CAS? Run CAS locally in a few short steps Quick Start](https://apereo.github.io/cas/development/planning/Quick-Start.html)

## Feature Summary

The following items include a summary of features and technologies presented by the CAS project:

- [Spring Webflow](https://apereo.github.io/cas/development/webflow/Webflow-Customization.html)/Spring Boot [Java server component](https://apereo.github.io/cas/development/planning/Architecture.html).
- [Pluggable authentication support](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Components.html) ([LDAP](https://apereo.github.io/cas/development/authentication/LDAP-Authentication.html), [Database](https://apereo.github.io/cas/development/authentication/Database-Authentication.html), [X.509](https://apereo.github.io/cas/development/authentication/X509-Authentication.html), [SPNEGO](https://apereo.github.io/cas/development/authentication/SPNEGO-Authentication.html), [JAAS](https://apereo.github.io/cas/development/authentication/JAAS-Authentication.html), [JWT](https://apereo.github.io/cas/development/authentication/JWT-Authentication.html), [RADIUS](https://apereo.github.io/cas/development/mfa/RADIUS-Authentication.html), [MongoDb](https://apereo.github.io/cas/development/authentication/MongoDb-Authentication.html), etc)
- Support for multiple protocols ([CAS](https://apereo.github.io/cas/development/protocol/CAS-Protocol.html), [SAML v1](https://apereo.github.io/cas/development/protocol/SAML-v1-Protocol.html), [SAML v2](https://apereo.github.io/cas/development/authentication/Configuring-SAML2-Authentication.html), [WS-Federation](https://apereo.github.io/cas/development/protocol/WS-Federation-Protocol.html), [OAuth2](https://apereo.github.io/cas/development/protocol/OAuth-Protocol.html), [OpenID Connect](https://apereo.github.io/cas/development/protocol/OIDC-Protocol.html), [REST](https://apereo.github.io/cas/development/protocol/REST-Protocol.html))
- Support for [multifactor authentication](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication.html) via a variety of providers ([Duo Security](https://apereo.github.io/cas/development/mfa/DuoSecurity-Authentication.html), [FIDO2 WebAuthN](https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication.html), [Google Authenticator](https://apereo.github.io/cas/development/mfa/GoogleAuthenticator-Authentication.html), [Inwebo](https://apereo.github.io/cas/development/mfa/Inwebo-Authentication.html), etc.)
- Support for [delegated authentication](https://apereo.github.io/cas/development/integration/Delegate-Authentication.html) to external identity providers such as [ADFS](https://apereo.github.io/cas/development/integration/ADFS-Integration.html), Facebook, X (Twitter), SAML2 IdPs, OIDC OPs, etc.
- Built-in support for [password management](https://apereo.github.io/cas/development/password_management/Password-Management.html), [notifications](https://apereo.github.io/cas/development/webflow/Webflow-Customization-Interrupt.html), [terms of use](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP.html) and [impersonation](https://apereo.github.io/cas/development/authentication/Surrogate-Authentication.html).
- Support for [attribute release](https://apereo.github.io/cas/development/integration/Attribute-Release.html) including [user consent](https://apereo.github.io/cas/development/integration/Attribute-Release-Consent.html).
- [Monitor and track](https://apereo.github.io/cas/development/monitoring/Monitoring-Statistics.html) application and system behavior, [statistics and metrics](https://apereo.github.io/cas/development/monitoring/Configuring-Metrics.html) in real-time.
- Manage and review [audits](https://apereo.github.io/cas/development/audits/Audits.html) and [logs](https://apereo.github.io/cas/development/logging/Logging.html) centrally, and publish data to a variety of downstream systems.
- Manage and register [client applications and services](https://apereo.github.io/cas/development/services/Service-Management.html) with specific authentication policies.
- [Cross-platform client support](https://apereo.github.io/cas/development/integration/CAS-Clients.html) (Java, .NET, PHP, Perl, Apache, etc).
- Built-in integrations with [many SAML2 service providers](https://apereo.github.io/cas/development/integration/Configuring-SAML-SP-Integrations.html).
- Support for many other types of integrations, such as [SCIM](https://apereo.github.io/cas/development/integration/SCIM-Provisioning.html), [reCAPTCHA](https://apereo.github.io/cas/development/integration/Configuring-Google-reCAPTCHA.html), [Swagger](https://apereo.github.io/cas/development/integration/Swagger-Integration.html), etc.

## Contribute

To learn how to contribute to the project, [please see this guide](https://apereo.github.io/cas/developer/Contributor-Guidelines.html).

## Getting Started

We recommend reading the following documentation in order to plan and execute a CAS deployment.

- [Architecture](https://apereo.github.io/cas/development/planning/Architecture.html)
- [Getting Started](https://apereo.github.io/cas/development/planning/Getting-Started.html)
- [Quick Start](https://apereo.github.io/cas/development/planning/Quick-Start.html)
- [Installation Requirements](https://apereo.github.io/cas/development/planning/Installation-Requirements.html)
- [Installation](https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html)
- [Blog](https://apereo.github.io/)
- [Release Schedule](https://github.com/apereo/cas/milestones)

## Powered By

CAS development is powered by the following tools, projects and services.

[![IntelliJ IDEA](https://github.com/apereo/cas/assets/1205228/11d83496-1abe-4f5a-b1e2-e313607cd595)](https://www.jetbrains.com/idea/)

IntelliJ IDE makes development a more productive and enjoyable experience

[![Spring Boot](https://github.com/apereo/cas/assets/1205228/854849e9-1b02-4218-8cf7-a4fa4e2b9aa2)](https://github.com/spring-projects/spring-boot/)

Spring Boot is Spring's convention-over-configuration solution for creating production-grade Spring applications.

[![YourKit](https://github.com/apereo/cas/assets/1205228/81bf79a8-3771-4439-bcb4-34cfbb94467c)](https://www.yourkit.com/)

YourKit, LLC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/) and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/), innovative and intelligent tools for profiling Java and .NET applications.

[![Gradle](https://github.com/apereo/cas/assets/1205228/2774ecf6-c60c-4a66-9f5c-2fe8baaa7825)](https://www.gradle.org/)

Gradle Build Tool is a fast, dependable, and adaptable open-source build automation tool with an elegant and extensible declarative build language. From mobile apps to microservices, from small startups to big enterprises, Gradle helps teams build, automate and deliver better software, faster.
