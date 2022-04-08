---
layout: default
title: CAS - Home
---

{% include variables.html %}

# Apereo CAS - Identity & Single Sign-On

Welcome to the home of the Apereo Central Authentication Service project, more commonly 
referred to as CAS. CAS is an enterprise multilingual single sign-on solution and identity provider for the web 
and attempts to be a comprehensive platform for your authentication and authorization needs.

CAS is an open and well-documented authentication protocol. The primary implementation of the protocol is 
an open-source Java server component by the same name hosted here, with support for a plethora of 
additional authentication protocols and features.

The following items include a summary of features and technologies presented by the CAS project:

* [Spring Webflow](webflow/Webflow-Customization.html)/Spring Boot [Java server component](planning/Architecture.html).
* [Pluggable authentication support](authentication/Configuring-Authentication-Components.html) ([LDAP](authentication/LDAP-Authentication.html), 
[Database](authentication/Database-Authentication.html), [X.509](authentication/X509-Authentication.html), [SPNEGO](authentication/SPNEGO-Authentication.html), 
[JAAS](authentication/JAAS-Authentication.html), [JWT](authentication/JWT-Authentication.html), 
[RADIUS](mfa/RADIUS-Authentication.html), [MongoDb](authentication/MongoDb-Authentication.html), etc)
* Support for multiple protocols ([CAS](protocol/CAS-Protocol.html), [SAML v1](protocol/SAML-Protocol.html), [SAML v2](authentication/Configuring-SAML2-Authentication.html), [WS-Federation](protocol/WS-Federation-Protocol.html),
[OAuth2](protocol/OAuth-Protocol.html), [OpenID](protocol/OpenID-Protocol.html), [OpenID Connect](protocol/OIDC-Protocol.html), [REST](protocol/REST-Protocol.html))
* Support for [multifactor authentication](mfa/Configuring-Multifactor-Authentication.html) via a variety of 
providers ([Duo Security](mfa/DuoSecurity-Authentication.html), [FIDO U2F](mfa/FIDO-U2F-Authentication.html), 
[YubiKey](mfa/YubiKey-Authentication.html), [FIDO2 WebAuthN](mfa/FIDO2-WebAuthn-Authentication.html), [Google Authenticator](mfa/GoogleAuthenticator-Authentication.html), [Authy](mfa/AuthyAuthenticator-Authentication.html), [Acceptto](mfa/Acceptto-Authentication.html), [Inwebo](mfa/Inwebo-Authentication.html), etc.)
* Support for [delegated authentication](integration/Delegate-Authentication.html) to external identity providers such as [ADFS](integration/ADFS-Integration.html), Facebook, Twitter, SAML2 IdPs, OIDC OPs, etc.
* Built-in support for [password management](password_management/Password-Management.html), [notifications](webflow/Webflow-Customization-Interrupt.html), [terms of use](webflow/Webflow-Customization-AUP.html) and [impersonation](authentication/Surrogate-Authentication.html).
* Support for [attribute release](integration/Attribute-Release.html) including [user consent](integration/Attribute-Release-Consent.html).
* [Monitor and track](monitoring/Monitoring-Statistics.html) application and system behavior, [statistics and metrics](monitoring/Configuring-Metrics.html) in real-time.
* Manage and review [audits](audits/Audits.html) and [logs](logging/Logging.html) centrally, and publish data to a variety of downstream systems.  
* Manage and register [client applications and services](services/Service-Management.html) with specific authentication policies.
* [Cross-platform client support](integration/CAS-Clients.html) (Java, .NET, PHP, Perl, Apache, etc).
* Integrations with [InCommon, Box, Office365, ServiceNow, Salesforce, Workday, WebAdvisor](integration/Configuring-SAML-SP-Integrations.html), Drupal, Blackboard, Moodle, [Google Apps](integration/Google-Apps-Integration.html), etc.
* Support for many other types of integrations, such as [SCIM](integration/SCIM-Integration.html), [reCAPTCHA](integration/Configuring-Google-reCAPTCHA.html), [Swagger](integration/Swagger-Integration.html), etc.

## Contribute

To learn how to contribute to the project, [please see this guide](/cas/developer/Contributor-Guidelines.html).

## Getting Started

We recommend reading the following documentation in order to plan and execute a CAS deployment.

* [Architecture](planning/Architecture.html)
* [Getting Started](planning/Getting-Started.html)
* [Installation Requirements](planning/Installation-Requirements.html)
* [Installation](installation/WAR-Overlay-Installation.html)
* [Blog](https://apereo.github.io)

## Powered By

CAS development is powered by the following tools, projects and services.

{:.list-group}
* {:.list-group-item} <a href="https://www.jetbrains.com/idea/"><img src="https://user-images.githubusercontent.com/1205228/31548576-1ac3d688-b038-11e7-9565-ffd89501872e.png" width="150"></a>
* {:.list-group-item} <a href="https://github.com/spring-projects/spring-boot"><img width="130" src="https://user-images.githubusercontent.com/1205228/32322526-0b58ac44-bfda-11e7-822e-ad763eb80faf.png"></a>
* {:.list-group-item} <a href="https://www.yourkit.com"><img src="https://user-images.githubusercontent.com/1205228/38207124-f6c6db34-36c1-11e8-9bbf-8dee5bd199c4.png" width="130"></a><br/>YourKit supports open source projects with its full-featured Java Profiler. YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a> and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>, innovative and intelligent tools for profiling Java and .NET applications.
