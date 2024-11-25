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

## Feature Summary

The following items include a summary of features and technologies presented by the CAS project:

* [Spring Webflow](webflow/Webflow-Customization.html)/Spring Boot [Java server component](planning/Architecture.html).
* [Pluggable authentication support](authentication/Configuring-Authentication-Components.html) ([LDAP](authentication/LDAP-Authentication.html), 
[Database](authentication/Database-Authentication.html), [X.509](authentication/X509-Authentication.html), [SPNEGO](authentication/SPNEGO-Authentication.html), 
[JAAS](authentication/JAAS-Authentication.html), [JWT](authentication/JWT-Authentication.html), 
[RADIUS](mfa/RADIUS-Authentication.html), [MongoDb](authentication/MongoDb-Authentication.html), etc)
* Support for multiple protocols ([CAS](protocol/CAS-Protocol.html), [SAML v1](protocol/SAML-v1-Protocol.html), [SAML v2](authentication/Configuring-SAML2-Authentication.html), [WS-Federation](protocol/WS-Federation-Protocol.html),
[OAuth2](protocol/OAuth-Protocol.html), [OpenID Connect](protocol/OIDC-Protocol.html), [REST](protocol/REST-Protocol.html))
* Support for [multifactor authentication](mfa/Configuring-Multifactor-Authentication.html) via a variety of 
providers ([Duo Security](mfa/DuoSecurity-Authentication.html), [FIDO2 WebAuthN](mfa/FIDO2-WebAuthn-Authentication.html), [Google Authenticator](mfa/GoogleAuthenticator-Authentication.html), [Inwebo](mfa/Inwebo-Authentication.html), etc.)
* Support for [delegated authentication](integration/Delegate-Authentication.html) to external identity providers such as [ADFS](integration/ADFS-Integration.html), Facebook, Twitter, SAML2 IdPs, OIDC OPs, etc.
* Built-in support for [password management](password_management/Password-Management.html), [notifications](webflow/Webflow-Customization-Interrupt.html), [terms of use](webflow/Webflow-Customization-AUP.html) and [impersonation](authentication/Surrogate-Authentication.html).
* Support for [attribute release](integration/Attribute-Release.html) including [user consent](integration/Attribute-Release-Consent.html).
* [Monitor and track](monitoring/Monitoring-Statistics.html) application and system behavior, [statistics and metrics](monitoring/Configuring-Metrics.html) in real-time.
* Manage and review [audits](audits/Audits.html) and [logs](logging/Logging.html) centrally, and publish data to a variety of downstream systems.  
* Manage and register [client applications and services](services/Service-Management.html) with specific authentication policies.
* [Cross-platform client support](integration/CAS-Clients.html) (Java, .NET, PHP, Perl, Apache, etc).
* Built-in integrations with [many SAML2 service providers](integration/Configuring-SAML-SP-Integrations.html).
* Support for many other types of integrations, such as [SCIM](integration/SCIM-Provisioning.html), [reCAPTCHA](integration/Configuring-Google-reCAPTCHA.html), 
  [Swagger](integration/Swagger-Integration.html), etc.

## Contribute

To learn how to contribute to the project, [please see this guide](/cas/developer/Contributor-Guidelines.html).

## Getting Started

We recommend reading the following documentation in order to plan and execute a CAS deployment.

* [Architecture](planning/Architecture.html)
* [Getting Started](planning/Getting-Started.html)
* [Installation Requirements](planning/Installation-Requirements.html)
* [Installation](installation/WAR-Overlay-Installation.html)
* [Blog](https://apereo.github.io)
* [Release Schedule](https://github.com/apereo/cas/milestones)

## Powered By

CAS development is powered by the following tools, projects and services.

<div class="row">
  <div class="col-sm-3 d-flex align-items-stretch">
    <div class="card border-0">
      <a href="https://www.jetbrains.com/idea/">
      <img src="https://github.com/apereo/cas/assets/1205228/11d83496-1abe-4f5a-b1e2-e313607cd595" class="card-img-top">
      </a>
      <div class="card-body">
        <p class="card-text">IntelliJ IDE makes development a more productive and enjoyable experience</p>
      </div>
    </div>
  </div>
  <div class="col-sm-3 d-flex align-items-stretch">
    <div class="card border-0">
      <a href="https://github.com/spring-projects/spring-boot/">
      <img src="https://github.com/apereo/cas/assets/1205228/854849e9-1b02-4218-8cf7-a4fa4e2b9aa2" class="card-img-top mt-2 ms-2 pe-4">
      </a>
      <div class="card-body">
        <p class="card-text">Spring Boot is Spring's convention-over-configuration solution for creating production-grade 
Spring applications.</p>
      </div>
    </div>
  </div>

  <div class="col-sm-3 d-flex align-items-stretch">
    <div class="card border-0">
      <a href="https://www.yourkit.com">
      <img src="https://github.com/apereo/cas/assets/1205228/81bf79a8-3771-4439-bcb4-34cfbb94467c" class="card-img-top mt-2 ms-2 pe-4">
      </a>
      <div class="card-body">
        <p class="card-text">
        YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a> 
and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>, innovative and intelligent 
tools for profiling Java and .NET applications.
        </p>
      </div>
    </div>
  </div>

<div class="col-sm-3 d-flex align-items-stretch">
    <div class="card border-0">
      <a href="https://www.gradle.org">
      <img src="https://github.com/apereo/cas/assets/1205228/2774ecf6-c60c-4a66-9f5c-2fe8baaa7825"
           style="filter: brightness(300%)" class="card-img-top mt-2 ms-2 pe-4">
      </a>
      <div class="card-body">
        <p class="card-text">
        Gradle Build Tool is a fast, dependable, and adaptable open-source build automation tool with an elegant and extensible declarative build language. 
From mobile apps to microservices, from small startups to big enterprises, Gradle helps teams build, automate and deliver better software, faster.
        </p>
      </div>
    </div>
  </div>
</div>
