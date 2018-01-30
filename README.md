<p align="center">
<img src="https://user-images.githubusercontent.com/1205228/30969994-e2fe6bf0-a470-11e7-80f9-d54d1e4d348e.png">
</p>

# Central Authentication Service (CAS)

[![ghit.me](https://ghit.me/badge.svg?repo=apereo/cas)](https://ghit.me/repo/apereo/cas)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/apereo/cas/blob/master/LICENSE)
[![Twitter](https://img.shields.io/badge/Apereo%20CAS-Twitter-blue.svg)](https://twitter.com/apereo)
[![Support](https://img.shields.io/badge/Support-Mailing%20Lists-green.svg?colorB=ff69b4)][cassupport]
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)][casgitter] 
[![Slack](https://img.shields.io/badge/Slack-join%20chat-blue.svg)][casslack]
[![Stack Overflow](http://img.shields.io/:stack%20overflow-cas-brightgreen.svg)](http://stackoverflow.com/questions/tagged/cas)

## Introduction

Welcome to the home of the [Central Authentication Service project](https://www.apereo.org/cas), more commonly referred to as CAS.
CAS is an enterprise multilingual single sign-on solution for the web and attempts to be a comprehensive platform for your authentication and authorization needs.

CAS is an open and well-documented authentication protocol. The primary implementation of the protocol is an open-source Java server component by the same name hosted here, with support for a plethora of additional authentication protocols and features.

## Contributions [![Contributing Guide](https://img.shields.io/badge/contributing-guide-green.svg?style=flat)][contribute]

- [How to contribute][contribute]

If you have already identified an enhancement or a bug, it is STRONGLY recommended that you simply submit a pull request to address the case. There is no need for special ceremony to create separate issues. The pull request IS the issue and it will be tracked and tagged as such.

## Documentation

[![Javadoc](https://img.shields.io/badge/Documentation-Javadoc-ff69b4.svg)](https://www.javadoc.io/doc/org.apereo.cas/cas-server)

- [Documentation][wiki]
- [Blog][blog]
- [Release Notes][releasenotes]
- [Support][cassupport]

## Features

The following features are supported by the CAS project:

* CAS v1, v2 and v3 Protocols
* SAML v1 and v2 Protocol
* OAuth Protocol
* OpenID & OpenID Connect Protocol
* WS-Federation Passive Requestor Protocol
* Authentication via JAAS, LDAP, RDBMS, X.509, Radius, SPNEGO, JWT, Remote, Trusted, BASIC, Apache Shiro, MongoDb, Pac4J and more.
* Delegated authentication to WS-FED, Facebook, Twitter, SAML IdP, OpenID, OpenID Connect, CAS and more.
* Authorization via ABAC, Time/Date, REST, Internet2's Grouper and more.
* HA clustered deployments via Hazelcast, Ehcache, JPA, Memcached, Apache Ignite, MongoDb, Redis, DynamoDb, Couchbase and more.
* Application registration backed by JSON, LDAP, YAML, JPA, Couchbase, MongoDb, DynamoDb, Redis and more.
* Multifactor authentication via Duo Security, YubiKey, RSA, Google Authenticator, Microsoft Azure and more.
* Administrative UIs to manage logging, monitoring, statistics, configuration, client registration and more.
* Global and per-application user interface theme and branding.
* Password management and password policy enforcement.

The foundations of CAS are built upon: [Spring Boot](https://projects.spring.io/spring-boot) and 
[Spring Cloud](http://projects.spring.io/spring-cloud/).

## Development

[![Dependency Status](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b) 
[![Codacy Badge](https://api.codacy.com/project/badge/grade/cc934b4c7d5d42d28e63757ff9e56d47)](http://bit.ly/1Uf6rwC) 
[![Scrutinizer Code Quality](https://scrutinizer-ci.com/g/apereo/cas/badges/quality-score.png?b=master)](https://scrutinizer-ci.com/g/apereo/cas/?branch=master)
[![CLA assistant](https://cla-assistant.io/readme/badge/apereo/cas)](https://cla-assistant.io/apereo/cas) 
[![Dependency Status](https://dependencyci.com/github/apereo/cas/badge)](https://dependencyci.com/github/apereo/cas) 
[![Coverage Status](https://coveralls.io/repos/github/apereo/cas/badge.svg?branch=master)](https://coveralls.io/github/apereo/cas?branch=master)

To build the project locally, please follow [this guide](https://apereo.github.io/cas/developer/Build-Process.html). The release schedule is [available here](https://github.com/apereo/cas/milestones).

## Deployment 

[![Build Status](https://api.travis-ci.org/apereo/cas.png?branch=master)](http://travis-ci.org/apereo/cas) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas/cas-server-webapp/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas/cas-server) 
[![Github Releases](https://img.shields.io/github/release/apereo/cas.svg)](https://github.com/apereo/cas/releases)

It is recommended to deploy CAS locally using the [WAR Overlay method][overlay]. Cloning or downloading the CAS codebase is *ONLY* required if you wish to contribute to the development of the project.

## Support

CAS is 100% free open source software managed by [Apereo](https://www.apereo.org/), licensed under [Apache v2](LICENSE). Our community has access to all releases of the CAS software with absolutely no costs. We welcome contributions from our community of all types and sizes. The time and effort to develop and maintain this project is dedicated by a group of [volunteers and contributors](https://github.com/apereo/cas/graphs/contributors). Support options may be [found here][cassupport]. If you (or your employer) benefit from this project, please consider becoming a [Friend of Apereo](https://www.apereo.org/friends) and contribute.

[wiki]: https://apereo.github.io/cas
[overlay]: https://apereo.github.io/cas/development/installation/Maven-Overlay-Installation.html
[contribute]: https://apereo.github.io/cas/developer/Contributor-Guidelines.html
[downloadcas]: http://www.apereo.org/cas/download
[cassonatype]: https://oss.sonatype.org/content/repositories/snapshots/org/apereo/cas/
[casmavencentral]: http://mvnrepository.com/artifact/org.apereo.cas
[downloadcasgithub]: https://github.com/apereo/cas/archive/master.zip
[releasenotes]: https://github.com/apereo/cas/releases
[cassupport]: https://apereo.github.io/cas/Support.html
[casgitter]: https://gitter.im/apereo/cas?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge
[casslack]: https://apereo.slack.com/
[blog]: https://apereo.github.io/
