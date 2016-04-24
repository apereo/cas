<p align="center">![][casimg]
# Central Authentication Service (CAS)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/Jasig/cas/blob/master/LICENSE)
[![](https://heroku-badge.herokuapp.com/?app=jasigcas)][caswebheroku]
[![](https://heroku-badge.herokuapp.com/?app=jasigcasmgmt)][casmgheroku] [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas/cas-server-webapp/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas/cas-server) [![Github Releases](https://img.shields.io/github/release/Jasig/cas.svg)](https://github.com/Jasig/cas/releases)
[![Stack Overflow](http://img.shields.io/:stack%20overflow-cas-brightgreen.svg)](http://stackoverflow.com/questions/tagged/cas)
[![Coverage Status](https://coveralls.io/repos/github/Jasig/cas/badge.svg?branch=master)](https://coveralls.io/github/Jasig/cas?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b)

## Introduction

Welcome to the home of the [Central Authentication Service project](http://www.apereo.org/cas), more commonly referred to as CAS. 
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
* OpenID Protocol
* OpenID Connect Protocol
* Authentication via JAAS, LDAP, RDBMS, X.509, Radius, SPNEGO, JWT Token, Stormpath, Remote, Trusted, BASIC, Apache Shiro, MongoDb, Pac4J and more.
* Delegated authentication to WS-FED, Facebook, Twitter, SAML v2 IdP, OpenID, OpenID Connect, CAS and more.
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

## Deployment [![Build Status](https://api.travis-ci.org/Jasig/cas.png?branch=master)](http://travis-ci.org/Jasig/cas) [![Issue Stats](http://www.issuestats.com/github/Jasig/cas/badge/pr?style=flat)](http://www.issuestats.com/github/Jasig/cas) [![Issue Stats](http://www.issuestats.com/github/Jasig/cas/badge/issue?style=flat)](http://www.issuestats.com/github/Jasig/cas)

It is recommended to build and deploy CAS locally using the [WAR Overlay method][overlay]. 
This approach does not require the adopter to *explicitly* download any version of CAS, but 
rather utilizes the overlay mechanism to combine CAS original artifacts and local 
customizations to further ease future upgrades and maintenance.

**Note: Do NOT clone or download the CAS codebase directly. That is ONLY required if you wish to contribute to the development of 
the project. Utilize the [WAR Overlay method](overlay) instead to build and deploy your CAS instance.**


## Contributions
- [How to contribute][contribute]

[wiki]: http://jasig.github.io/cas
[overlay]: http://jasig.github.io/cas/development/installation/Maven-Overlay-Installation.html
[contribute]: http://jasig.github.io/cas/developer/Contributor-Guidelines.html
[downloadcas]: http://www.apereo.org/cas/download
[cassonatype]: https://oss.sonatype.org/content/repositories/snapshots/org/jasig/cas/
[casmavencentral]: http://mvnrepository.com/artifact/org.jasig.cas
[downloadcasgithub]: https://github.com/Jasig/cas/archive/master.zip
[releasenotes]: https://github.com/Jasig/cas/releases
[casimg]: https://www.apereo.org/sites/default/files/styles/project_logo/public/projects/logos/cas_max_logo_0.png?itok=uD4hQ5-h
[caswebheroku]: http://jasigcas.heroku.com
[casmgheroku]: http://jasigcasmgmt.heroku.com
[cassupport]: http://jasig.github.io/cas/Support.html
[casgitter]: https://gitter.im/Jasig/cas?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge

