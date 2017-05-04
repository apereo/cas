![][casimg]
# Central Authentication Service (CAS)
[![ghit.me](https://ghit.me/badge.svg?repo=apereo/cas)](https://ghit.me/repo/apereo/cas)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/apereo/cas/blob/master/LICENSE)

## Introduction

Welcome to the home of the [Central Authentication Service project](https://www.apereo.org/cas), more commonly referred to as CAS. 
CAS provides enterprise single sign-on for the web and attempts to be a comprehensive platform for your authentication and authorization needs. 

CAS is an open and well-documented authentication protocol. The primary implementation of the protocol is 
an open-source Java server component by the same name, hosted here. 

## Contributions [![Contributing Guide](https://img.shields.io/badge/contributing-guide-green.svg?style=flat)][contribute]

- [How to contribute][contribute]

## Features

The following features are supported by the CAS project:

* CAS v1, v2 and v3 Protocol
* SAML v1 and v2 Protocol
* OAuth Protocol
* OpenID & OpenID Connect Protocol
* WS-Federation Passive Requestor Protocol
* Authentication via JAAS, LDAP, RDBMS, X.509, Radius, SPNEGO, JWT, Stormpath, Remote, Trusted, BASIC, Apache Shiro, MongoDb, Pac4J and more.
* Delegated authentication to WS-FED, Facebook, Twitter, SAML IdP, OpenID, OpenID Connect, CAS and more.
* Authorization via ABAC, Time/Date, REST, Internet2's Grouper and more.
* HA clustered deployments via Hazelcast, Ehcache, JPA, Memcached, Apache Ignite, MongoDb, Redis, Couchbase and more.
* Application registration backed by JSON, LDAP, YAML, JPA, Couchbase, MongoDb and more.
* Multifactor authentication via Duo Security, YubiKey, RSA, Google Authenticator and more.
* Administrative UIs to manage logging, monitoring, statistics, configuration, client registration and more.
* Global and per-application user interface theme and branding.
* Password management and password policy enforcement.

The foundations of CAS are built upon: [Spring Boot](https://projects.spring.io/spring-boot), 
[Spring Cloud](http://projects.spring.io/spring-cloud/) and [Thymeleaf](http://thymeleaf.org).

## Development [![Dependency Status](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b) [![Codacy Badge](https://api.codacy.com/project/badge/grade/cc934b4c7d5d42d28e63757ff9e56d47)](http://bit.ly/1Uf6rwC) [![CLA assistant](https://cla-assistant.io/readme/badge/apereo/cas)](https://cla-assistant.io/apereo/cas) [![Dependency Status](https://dependencyci.com/github/apereo/cas/badge)](https://dependencyci.com/github/apereo/cas) 

To build the project locally, please follow [this guide](https://apereo.github.io/cas/developer/Build-Process.html).

## Documentation [![Gitter](https://badges.gitter.im/Join%20Chat.svg)][casgitter] [![Stack Overflow](http://img.shields.io/:stack%20overflow-cas-brightgreen.svg)](http://stackoverflow.com/questions/tagged/cas) [![JavaDoc](https://javadoc-emblem.rhcloud.com/doc/org.apereo.cas/cas-server/badge.svg)](http://www.javadoc.io/doc/org.apereo.cas/cas-server)

- [Documentation][wiki]
- [Release Notes][releasenotes]
- [Support][cassupport]

## Deployment [![Build Status](https://api.travis-ci.org/apereo/cas.png?branch=master)](http://travis-ci.org/apereo/cas) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas/cas-server-webapp/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas/cas-server) [![Github Releases](https://img.shields.io/github/release/apereo/cas.svg)](https://github.com/apereo/cas/releases)

It is recommended to build and deploy CAS locally using the [WAR Overlay method][overlay]. 
This approach does not require the adopter to *explicitly* download any version of CAS, but 
rather utilizes the overlay mechanism to combine CAS original artifacts and local 
customizations to further ease future upgrades and maintenance.

**Note: Do NOT clone or download the CAS codebase directly. That is ONLY required if you wish to contribute to the development of 
the project. Utilize the [WAR Overlay method][overlay] instead to build and deploy your CAS instance.**

[wiki]: https://apereo.github.io/cas
[overlay]: https://apereo.github.io/cas/development/installation/Maven-Overlay-Installation.html
[contribute]: https://apereo.github.io/cas/developer/Contributor-Guidelines.html
[downloadcas]: http://www.apereo.org/cas/download
[cassonatype]: https://oss.sonatype.org/content/repositories/snapshots/org/jasig/cas/
[casmavencentral]: http://mvnrepository.com/artifact/org.apereo.cas
[downloadcasgithub]: https://github.com/apereo/cas/archive/master.zip
[releasenotes]: https://github.com/apereo/cas/releases
[casimg]: https://cloud.githubusercontent.com/assets/1205228/14939607/7cd35c3c-0f02-11e6-9564-80d8dfc0a064.png
[cassupport]: https://apereo.github.io/cas/Support.html
[casgitter]: https://gitter.im/apereo/cas?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge

