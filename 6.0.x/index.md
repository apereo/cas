---
layout: default
title: CAS - Home
---

# CAS Enterprise Single Sign-On

* [Spring Webflow](webflow/Webflow-Customization.html)/Spring Boot [Java server component](planning/Architecture.html).
* [Pluggable authentication support](installation/Configuring-Authentication-Components.html) ([LDAP](installation/LDAP-Authentication.html), 
[Database](installation/Database-Authentication.html), [X.509](installation/X509-Authentication.html), [SPNEGO](installation/SPNEGO-Authentication.html), 
[JAAS](installation/JAAS-Authentication.html), [JWT](installation/JWT-Authentication.html), 
[RADIUS](mfa/RADIUS-Authentication.html), [MongoDb](installation/MongoDb-Authentication.html), etc)
* Support for multiple protocols ([CAS](protocol/CAS-Protocol.html), [SAML](protocol/SAML-Protocol.html), [WS-Federation](protocol/WS-Federation-Protocol.html),
[OAuth2](protocol/OAuth-Protocol.html), [OpenID](protocol/OpenID-Protocol.html), [OpenID Connect](protocol/OIDC-Protocol.html), [REST](protocol/REST-Protocol.html))
* Support for [multifactor authentication](mfa/Configuring-Multifactor-Authentication.html) via a variety of 
providers ([Duo Security](mfa/DuoSecurity-Authentication.html), [FIDO U2F](mfa/FIDO-U2F-Authentication.html), 
[YubiKey](mfa/YubiKey-Authentication.html), [Google Authenticator](mfa/GoogleAuthenticator-Authentication.html), [Authy](mfa/AuthyAuthenticator-Authentication.html) etc)
* Support for [delegated authentication](integration/Delegate-Authentication.html) to external providers such as [ADFS](integration/ADFS-Integration.html), Facebook, Twitter, SAML2 IdPs, etc.
* Built-in support for [password management](password_management/Password-Management.html), [notifications](webflow/Webflow-Customization-Interrupt.html), [terms of use](webflow/Webflow-Customization-AUP.html) and [impersonation](installation/Surrogate-Authentication.html).
* Support for [attribute release](integration/Attribute-Release.html) including [user consent](integration/Attribute-Release-Consent.html).
* [Monitor and track](monitoring/Monitoring-Statistics.html) application behavior, statistics and logs in real time.
* Manage and register [client applications and services](services/Service-Management.html) with specific authentication policies.
* [Cross-platform client support](integration/CAS-Clients.html) (Java, .Net, PHP, Perl, Apache, etc).
* Integrations with [InCommon, Box, Office365, ServiceNow, Salesforce, Workday, WebAdvisor](integration/Configuring-SAML-SP-Integrations.html), Drupal, Blackboard, Moodle, [Google Apps](integration/Google-Apps-Integration.html), etc.

## Contribute

To learn how to contribute to the project, [please see this guide](/cas/developer/Contributor-Guidelines.html).

## Getting Started

We recommend reading the following documentation in order to plan and execute a CAS deployment.

* [Architecture](planning/Architecture.html)
* [Getting Started](planning/Getting-Started.html)
* [Installation Requirements](planning/Installation-Requirements.html)
* [Installation](installation/WAR-Overlay-Installation.html)

## Demos

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>It is important to note that these are public demo sites, used by the project for basic showcases 
and integration tests. They are <strong>NOT</strong> set up for internal demos as they may go up and down as the project needs without notice. </p></div>

The following demos are provided by the Apereo CAS project:

| Topic                                                                                      | Source Branch            | Location | Heroku
|-------------------------------------|----------------------|--------------------------------------------------------|---------------------------------------
| [CAS Overlay Project Initializr](installation/WAR-Overlay-Installation.html)            | `heroku-casinitializr`   | [Link](https://casinitializr.herokuapp.com) | ![](https://heroku-badge.herokuapp.com/?app=casinitializr)
| [CAS Web Application Server](index.html)                                                  | `heroku-caswebapp`       | [Link](https://casserver.herokuapp.com/cas) | ![](https://heroku-badge.herokuapp.com/?app=casserver&root=cas)
| [CAS Management Server](services/Installing-ServicesMgmt-Webapp.html)        | `heroku-mgmtwebapp` | [Link](https://casservermgmt.herokuapp.com/cas-management)  | ![](https://heroku-badge.herokuapp.com/?app=casservermgmt&root=cas-management)
| [CAS Boot Administration Server](monitoring/Configuring-Monitoring-Administration.html) | `heroku-bootadminserver` | [Link](https://casbootadminserver.herokuapp.com/) | ![](https://heroku-badge.herokuapp.com/?app=casbootadminserver)
| [CAS Zipkin Server](monitoring/Monitoring-Statistics.html#distributed-tracing)          | `heroku-zipkinserver`    | [Link](https://caszipkinserver.herokuapp.com/) | ![](https://heroku-badge.herokuapp.com/?app=caszipkinserver)
| [CAS Service Discovery Server](installation/Service-Discovery-Guide.html)                 | `heroku-discoveryserver` | [Link](https://caseureka.herokuapp.com/) | ![](https://heroku-badge.herokuapp.com/?app=caseureka)
| [CAS Configuration Server](configuration/Configuration-Server-Management.html)             | `heroku-casconfigserver` | [Link](https://casconfigserver.herokuapp.com/casconfigserver/env) | ![](https://heroku-badge.herokuapp.com/?app=casconfigserver&root=casconfigserver)
| CAS GitHub Repository Bot | `heroku-githubbot` | [Link](https://apereocas-githubbot.herokuapp.com/) | ![](https://heroku-badge.herokuapp.com/?app=apereocas-githubbot)

Credentials used for the above demos, where needed, are: `casuser` / `Mellon`.

## Powered By

CAS development is powered by the following tools, projects and services.

{:.list-group}
* {:.list-group-item} <a href="https://travis-ci.org"><img src="https://travis-ci.com/images/logos/TravisCI-Full-Color.png" width="130"></a>
* {:.list-group-item} <a href="https://www.jetbrains.com/idea/"><img src="https://user-images.githubusercontent.com/1205228/31548576-1ac3d688-b038-11e7-9565-ffd89501872e.png" width="150"></a>
* {:.list-group-item} <a href="https://www.eclipse.org"><img width="130" src="https://user-images.githubusercontent.com/1205228/32225495-ac7b1e94-be5a-11e7-8f83-5c7399398fb8.png"></a>
* {:.list-group-item} <a href="http://projects.spring.io/spring-boot/"><img width="130" src="https://user-images.githubusercontent.com/1205228/32322526-0b58ac44-bfda-11e7-822e-ad763eb80faf.png"></a>
* {:.list-group-item} <a href="https://www.yourkit.com"><img src="https://user-images.githubusercontent.com/1205228/38207124-f6c6db34-36c1-11e8-9bbf-8dee5bd199c4.png" width="130"></a><br/>YourKit supports open source projects with its full-featured Java Profiler. YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a> and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>, innovative and intelligent tools for profiling Java and .NET applications.
