---
layout: default
title: CAS - Home
---

# Enterprise Single Sign-On

* Spring Webflow/Spring Boot [Java server component](planning/Architecture.html).
* [Pluggable authentication support](Configuring-Authentication-Components.html) ([LDAP](installation/LDAP-Authentication.html), 
[Database](installation/Database-Authentication.html), [X.509](installation/X509-Authentication.html), [SPNEGO](installation/SPNEGO-Authentication.html), 
[JAAS](installation/JAAS-Authentication.html), [JWT](installation/JWT-Authentication.html), 
[RADIUS](installation/RADIUS-Authentication.html), [MongoDb](installation/MongoDb-Authentication.html), etc)
* Support for multiple protocols ([CAS](protocol/CAS-Protocol.html), [SAML](protocol/SAML-Protocol.html), 
[OAuth2](protocol/OAuth-Protocol.html), [OpenID](protocol/OpenID-Protocol.html), [OpenID Connect](protocol/OIDC-Protocol.html))
* Support for [multifactor authentication](installation/Configuring-Multifactor-Authentication.html) via a variety of 
providers ([Duo Security](installation/DuoSecurity-Authentication.html), [FIDO U2F](installation/FIDO-U2F-Authentication.html), 
[YubiKey](installation/YubiKey-Authentication.html), [Google Authenticator](installation/GoogleAuthenticator-Authentication.html), etc)
* Support for [delegated authentication](integration/Delegate-Authentication.html) to external providers such as [ADFS](integration/ADFS-Integration.html), Facebook, Twitter, SAML2 IdPs, etc.
* [Monitor and track](installation/Monitoring-Statistics.html) application behavior, statistics and logs in real time.
* Manage and register [client applications and services](installation/Service-Management.html) with specific authentication policies.
* [Cross-platform client support](integration/CAS-Clients.html) (Java, .Net, PHP, Perl, Apache, etc).
* Integrations with [InCommon, Box, Office365, ServiceNow, Salesforce, Workday, WebAdvisor](integration/Configuring-SAML-SP-Integrations.html), Drupal, Blackboard, Moodle, [Google Apps](integration/Google-Apps-Integration.html), etc.

CAS provides a friendly open source community that actively supports and contributes to the project.
While the project is rooted in higher-ed open source, it has grown to an international audience spanning
Fortune 500 companies and small special-purpose installations.

## Getting Started

We recommend reading the following documentation in order to plan and execute a CAS deployment.

* [Architecture](planning/Architecture.html)
* [Getting Started](planning/Getting-Started.html)
* [Installation Requirements](planning/Installation-Requirements.html)
* [Overlay Installation](installation/Maven-Overlay-Installation.html)
* [Authentication](installation/Configuring-Authentication-Components.html)
* [Application Registration](installation/Service-Management.html)
* [Attribute Release](integration/Attribute-Release.html)

## Demo

The CAS web application is available for demo at [https://jasigcas.herokuapp.com/cas](https://jasigcas.herokuapp.com/cas)

It is important to note that this is a public demo site, used by the project for basic showcases and integration tests. It is **NOT** set up for internal demos and it may go down and up as the project needs without notice. If you have a need for a demo instance with a modified UI, that would be one you set up for your deployment. 

## Development

CAS development is powered by: <br/>

<a href="http://www.jetbrains.com/idea/" target="_blank"><img src="../images/intellijidea.gif" valign="middle" style="vertical-align:middle"></a>

