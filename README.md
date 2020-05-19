<p align="center">
<img src="https://user-images.githubusercontent.com/2813838/66173345-91b00380-e604-11e9-95f4-546767cc134c.png">
</p>

# Central Authentication Service (CAS)

[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/apereo/cas/blob/master/LICENSE)
[![Twitter](https://img.shields.io/badge/Apereo%20CAS-Twitter-blue.svg)](https://twitter.com/apereo)
[![Support](https://img.shields.io/badge/Support-Mailing%20Lists-green.svg?colorB=ff69b4)][cassupport]
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)][casgitter]
[![Slack](https://img.shields.io/badge/Slack-join%20chat-blue.svg)][casslack]
[![Stack Overflow](http://img.shields.io/:stack%20overflow-cas-brightgreen.svg)](http://stackoverflow.com/questions/tagged/cas)

## Introduction

Welcome to the home of the [Central Authentication Service project](https://www.apereo.org/cas), more commonly referred to as CAS. CAS is an
enterprise multilingual single sign-on solution for the web and attempts to be a comprehensive platform for your authentication and authorization needs.

CAS is an open and well-documented authentication protocol. The primary implementation of the protocol is an open-source Java server
component by the same name hosted here, with support for a plethora of additional authentication protocols and features.

## Contributions

[![Contributing Guide](https://img.shields.io/badge/Contributions-guide-green.svg?style=flat)][contribute]
[![Open Pull Requests](https://img.shields.io/github/issues-pr/apereo/cas.svg?style=flat)][contribute]

- [How to contribute][contribute]

If you have already identified an enhancement or a bug, it is STRONGLY recommended that you simply submit a pull request to address the case.
There is no need for special ceremony to create separate issues. The pull request IS the issue and it will be tracked and tagged as such.

## Documentation [![Javadoc](https://img.shields.io/badge/Documentation-Javadoc-ff69b4.svg)](https://www.javadoc.io/doc/org.apereo.cas/cas-server-core)

| Version         | Reference
|------------|-----------------------------------
| ![](https://img.shields.io/badge/Development-WIP-blue.svg?style=flat) | [Link](https://apereo.github.io/cas/development)
| ![](https://img.shields.io/badge/6.1.x-Current-green.svg?style=flat) | [Link](https://apereo.github.io/cas/6.1.x)
| ![](https://img.shields.io/badge/6.0.x-Current-green.svg?style=flat) | [Link](https://apereo.github.io/cas/6.0.x)

Additional resources are available as follows:

- [Apereo Blog][blog]
- [Release Notes][releasenotes]
- [Support][cassupport]
- [Maintenance Policy][maintenance]
- [Release Schedule][releaseschedule]

## Deployment

[![Contributing Guide](https://img.shields.io/badge/Contributions-guide-green.svg?style=flat)][contribute]
[![Open Pull Requests](https://img.shields.io/github/issues-pr/apereo/cas.svg?style=flat)][contribute]
[![codecov](https://codecov.io/gh/apereo/cas/branch/master/graph/badge.svg)](https://codecov.io/gh/apereo/cas)
![CAS Build](https://github.com/apereo/cas/workflows/Build/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas/cas-server-core/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.apereo.cas/cas-server-core)
[![Github Releases](https://img.shields.io/github/release/apereo/cas.svg)](https://github.com/apereo/cas/releases)

It is recommended to deploy CAS locally using the [WAR Overlay method][overlay]. Cloning or downloading the CAS codebase
is **ONLY** required if you wish to contribute to the development of the project.

## Features

The following features are supported by the CAS project:

* CAS v1, v2 and v3 Protocol
* SAML v1 and v2 Protocol
* OAuth v2 Protocol
* OpenID & OpenID Connect Protocol
* WS-Federation Passive Requestor Protocol
* Authentication via JAAS, LDAP, RDBMS, X.509, Radius, SPNEGO, JWT, Remote, Apache Cassandra, Trusted, BASIC, Apache Shiro, MongoDb, Pac4J and more.
* Delegated authentication to WS-FED, Facebook, Twitter, SAML IdP, OpenID, OpenID Connect, CAS and more.
* Authorization via ABAC, Time/Date, REST, Internet2's Grouper and more.
* HA clustered deployments via Hazelcast, Ehcache, JPA, Apache Cassandra, Memcached, Apache Ignite, MongoDb, Redis, DynamoDb, Couchbase and more.
* Application registration backed by JSON, LDAP, YAML, Apache Cassandra, JPA, Couchbase, MongoDb, DynamoDb, Redis and more.
* Multifactor authentication via Duo Security, YubiKey, RSA, Google Authenticator and more.
* Administrative UIs to manage logging, monitoring, statistics, configuration, client registration and more.
* Global and per-application user interface theme and branding.
* Password management and password policy enforcement.
* Deployment options using Apache Tomcat, Jetty, Undertow, packaged and running as Docker containers.

The foundations of CAS are built upon: [Spring Boot](https://projects.spring.io/spring-boot) and
[Spring Cloud](http://projects.spring.io/spring-cloud/).

## Development

[![Codacy Badge](https://api.codacy.com/project/badge/grade/cc934b4c7d5d42d28e63757ff9e56d47)](http://bit.ly/1Uf6rwC)
[![CLA assistant](https://cla-assistant.io/readme/badge/apereo/cas)](https://cla-assistant.io/apereo/cas)
[![Sonarqube Quality](https://sonarcloud.io/api/project_badges/measure?project=org.apereo.cas%3Acas-server&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.apereo.cas%3Acas-server)
[![Sonarqube Quality](https://sonarcloud.io/api/project_badges/measure?project=org.apereo.cas%3Acas-server&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=org.apereo.cas%3Acas-server)

To build the project locally, please follow [this guide](https://apereo.github.io/cas/developer/Build-Process.html).
The release schedule is [available here][releaseschedule].

## Support

CAS is 100% free open source software managed by [Apereo](https://www.apereo.org/), licensed under [Apache v2](LICENSE). Our
community has access to all releases of the CAS software with absolutely no costs. We welcome contributions from our community of all
types and sizes. The time and effort to develop and maintain this project is dedicated by a group
of [volunteers and contributors](https://github.com/apereo/cas/graphs/contributors). Support options may be [found here][cassupport].
If you (or your employer) benefit from this project, please consider becoming a [Friend of Apereo](https://www.apereo.org/friends) and contribute.

[maintenance]: https://apereo.github.io/cas/developer/Maintenance-Policy.html
[releaseschedule]: https://github.com/apereo/cas/milestones
[wiki]: https://apereo.github.io/cas
[overlay]: https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html
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
