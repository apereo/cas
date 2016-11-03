![][casimg]
# Central Authentication Service (CAS)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/apereo/cas/blob/master/LICENSE)
[![](https://heroku-badge.herokuapp.com/?app=jasigcas&root=/cas/login)][caswebheroku]
[![](https://heroku-badge.herokuapp.com/?app=jasigcasmgmt&root=/cas-services/login)][casmgheroku] [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas/cas-server-webapp/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas/cas-server) [![Github Releases](https://img.shields.io/github/release/apereo/cas.svg)](https://github.com/apereo/cas/releases)
[![Stack Overflow](http://img.shields.io/:stack%20overflow-cas-brightgreen.svg)](http://stackoverflow.com/questions/tagged/cas)
[![Dependency Status](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b)
[![JavaDoc](https://javadoc-emblem.rhcloud.com/doc/org.apereo.cas/cas-server/badge.svg)](http://www.javadoc.io/doc/org.apereo.cas/cas-server)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/cc934b4c7d5d42d28e63757ff9e56d47)](http://bit.ly/1Uf6rwC)
[![CLA assistant](https://cla-assistant.io/readme/badge/apereo/cas)](https://cla-assistant.io/apereo/cas)
[![Dependency Status](https://dependencyci.com/github/apereo/cas/badge)](https://dependencyci.com/github/apereo/cas)

## Introduction

Welcome to the home of the [Central Authentication Service project](https://www.apereo.org/cas), more commonly referred to as CAS. 
CAS provides enterprise single sign-on for the web and attempts to be a comprehensive platform for your authentication and authorization needs. 

CAS provides an extensive list of features for most application requirements and integrations, offers community documentation 
and implementation support, has an extensive community of adopters and is virtually supported by most programming languages, frameworks and products.

CAS is an open and well-documented authentication protocol. The primary implementation of the protocol is 
an open-source Java server component by the same name, hosted here. 

## Features
The following features are supported by the CAS project:

* CAS v1, v2 and v3 Protocol
* SAML v1 and v2 Protocol
* OAuth Protocol
* OpenID & OpenID Connect Protocol
* Authentication via JAAS, LDAP, RDBMS, X.509, Radius, SPNEGO, JWT, Stormpath, Remote, Trusted, BASIC, Apache Shiro, MongoDb, Pac4J and more.
* Delegated authentication to WS-FED, Facebook, Twitter, SAML IdP, OpenID, OpenID Connect, CAS and more.
* Authorization via ABAC, Time/Date, Internet2's Grouper and more.
* HA clustered deployments via Hazelcast, Ehcache, JPA, Memcached, Apache Ignite, Couchbase and more.
* Application registration backed by JSON, LDAP, JPA, Couchbase, MongoDb and more.
* Multifactor authentication via Duo Security, YubiKey, RSA, Google Authenticator and more.
* Administrative UIs to manage logging, monitoring, statistics, configuration, client registration and more.
* Global and per-application user interface theme and branding.

The foundations of CAS are built upon: Spring Framework, Spring Boot, Spring Cloud, Spring Session and Thymeleaf.

## Documentation [![Gitter](https://badges.gitter.im/Join Chat.svg)][casgitter]
- [Documentation][wiki]
- [Release Notes][releasenotes]
- [Support] [cassupport]

## Deployment [![Build Status](https://api.travis-ci.org/apereo/cas.png?branch=master)](http://travis-ci.org/apereo/cas)

It is recommended to build and deploy CAS locally using the [WAR Overlay method][overlay]. 
This approach does not require the adopter to *explicitly* download any version of CAS, but 
rather utilizes the overlay mechanism to combine CAS original artifacts and local 
customizations to further ease future upgrades and maintenance.

**Note: Do NOT clone or download the CAS codebase directly. That is ONLY required if you wish to contribute to the development of 
the project. Utilize the [WAR Overlay method][overlay] instead to build and deploy your CAS instance.**

## Contributions

- [How to contribute][contribute]

## Development

CAS development is powered by: <br/>

<a href="http://www.jetbrains.com/idea/" target="_blank"><img src="https://apereo.github.io/cas/images/intellijidea.gif" valign="middle" style="vertical-align:middle"></a>

[wiki]: http://apereo.github.io/cas
[overlay]: http://apereo.github.io/cas/development/installation/Maven-Overlay-Installation.html
[contribute]: http://apereo.github.io/cas/developer/Contributor-Guidelines.html
[downloadcas]: http://www.apereo.org/cas/download
[cassonatype]: https://oss.sonatype.org/content/repositories/snapshots/org/jasig/cas/
[casmavencentral]: http://mvnrepository.com/artifact/org.apereo.cas
[downloadcasgithub]: https://github.com/apereo/cas/archive/master.zip
[releasenotes]: https://github.com/apereo/cas/releases
[casimg]: https://cloud.githubusercontent.com/assets/1205228/14939607/7cd35c3c-0f02-11e6-9564-80d8dfc0a064.png
[caswebheroku]: http://jasigcas.heroku.com/cas
[casmgheroku]: http://jasigcasmgmt.heroku.com/cas-services
[cassupport]: http://apereo.github.io/cas/Support.html
[casgitter]: https://gitter.im/apereo/cas?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge

