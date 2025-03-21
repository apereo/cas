<p align="center">
<img src="https://github.com/user-attachments/assets/c2daa28c-cdfb-42a7-8333-db967cc3cce7">
</p>

# Central Authentication Service (CAS)

[![License](https://img.shields.io/hexpm/l/plug.svg?style=for-the-badge&logo=apache)](https://github.com/apereo/cas/blob/master/LICENSE)
[![Slack](https://img.shields.io/badge/Slack-join%20chat-blue.svg?style=for-the-badge&logo=slack)][casslack]
[![Support](https://img.shields.io/badge/Support-Mailing%20Lists-green.svg?colorB=ff69b4&style=for-the-badge)][cassupport]

## Introduction

Welcome to the home of the [Central Authentication Service project][apereocas], more commonly referred to as CAS. CAS is an
enterprise multilingual identity provider and single sign-on solution for the web and attempts to 
be a comprehensive platform for your authentication and authorization needs.

CAS is an open and well-documented authentication protocol. The primary implementation of the protocol is an open-source Java server
component by the same name hosted here, with support for a plethora of additional authentication protocols and features such a SAML2, OpenID Connect, MFA 
and many many more.

## Contributions

[![Contributing Guide](https://img.shields.io/badge/Contributions-guide-green.svg?style=for-the-badge&logo=github)][contribute]
[![Open Pull Requests](https://img.shields.io/github/issues-pr/apereo/cas.svg?style=for-the-badge&logo=github)][contribute]

- [How to contribute][contribute]

If you have already identified an enhancement or a bug, it is STRONGLY recommended that you submit a pull request to address the case.
There is no need for special ceremony to create separate issues. The pull request IS the issue and it will be tracked and tagged as such.

<a href="https://github.com/apereo/cas/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=apereo/cas" alt="Contributors"/>
</a>

## Documentation [![Javadoc](https://img.shields.io/badge/Documentation-Javadoc-ff69b4.svg?style=for-the-badge&logo=readme)][casjavadocs]

| Version                                                                                    | Reference                                        |
|--------------------------------------------------------------------------------------------|--------------------------------------------------|
| ![](https://img.shields.io/badge/Development-WIP-blue.svg?style=for-the-badge&logo=github) | [Link](https://apereo.github.io/cas/development) |
| ![](https://img.shields.io/badge/7.2.x-Current-green.svg?style=for-the-badge&logo=github)  | [Link](https://apereo.github.io/cas/7.2.x)       |
| ![](https://img.shields.io/badge/7.1.x-Current-green.svg?style=for-the-badge&logo=github)  | [Link](https://apereo.github.io/cas/7.1.x)       |

Additional resources are available as follows:

- [Apereo Blog][blog]
- [Release Notes][releasenotes]
- [Support][cassupport]
- [Maintenance Policy][maintenance]
- [Release Schedule][releaseschedule]

## Getting Started

[![Maven Central](https://img.shields.io/maven-central/v/org.apereo.cas/cas-server-webapp?style=for-the-badge&logo=apachemaven)][casmavencentral]
[![GitHub Releases](https://img.shields.io/github/release/apereo/cas.svg?style=for-the-badge&logo=github)][githubreleases]

It is recommended to deploy CAS locally using the [WAR Overlay method][overlay]. Cloning or downloading the CAS codebase
is **ONLY** required if you wish to contribute to the development of the project.

We recommend that you review [this page][gettingstarted] to get started with your CAS deployment.

## Features

The following features are supported by the CAS project:

* CAS v1, v2 and v3 Protocol
* SAML v1 and v2 Protocol
* OAuth v2 Protocol
* OpenID Connect Protocol
* WS-Federation Passive Requestor Protocol
* Authentication via JAAS, LDAP, RDBMS, X.509, Radius, SPNEGO, JWT, Remote, Apache Cassandra, Trusted, BASIC, MongoDB and more.
* Delegated (social) authentication to external identity providers such as WS-FED, SAML2, OpenID Connect, OAuth CAS and more.
* Authorization via Heimdall, OpenFGA, OPA, ABAC, Time/Date, REST, Internet2's Grouper and more.
* HA clustered deployments via Hazelcast, JPA, Hazelcast, Memcached, Apache Ignite, MongoDB, Redis, DynamoDb, and more.
* Application registration backed by JSON, LDAP, YAML, Google Cloud, JPA, MongoDB, DynamoDb, Redis and more.
* Multifactor authentication via Duo Security, Simple MFA, YubiKey, RSA, Google Authenticator, WebAuthn FIDO2 and more.
* Administrative UIs to manage logging, monitoring, statistics, configuration, client registration and more.
* Email and SMS notification options via Twilio, Mailgun, SendGrid, Amazon SES and more.
* User attribute consent and management via LDAP, RDBMS, MongoDB, DynamoDb and more.
* Global and per-application user interface theme and branding.
* Password management and password policy enforcement.
* Integration options with Apache Syncope, SCIM, Swagger, Shibboleth IdP, Keycloak, Okta, and more.
* Deployment options using Apache Tomcat, Jetty, Undertow, packaged and running as Docker containers.

The foundations of CAS are built upon: [Spring Boot][springboot] and
[Spring Cloud][springcloud].

## Development [![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)][devlocity] [![codecov](https://codecov.io/gh/apereo/cas/branch/master/graph/badge.svg?style=for-the-badge)][cascodecov]

- To build the project locally, please follow [this guide][casbuildprocess].
- The release schedule is [available here][releaseschedule].

## Support

Apereo CAS is 100% free open source software managed by [Apereo](https://www.apereo.org/), licensed under [Apache v2](LICENSE). Our
community has access to all releases of the CAS software with absolutely no costs. We welcome contributions from our community of all
types and sizes. The time and effort to develop and maintain this project is dedicated by a group
of [volunteers and contributors][githubcontributors]. 
If you (or your employer) benefit from this project, please consider becoming a [Friend of Apereo](https://www.apereo.org/friends) and contribute.

Commercial support options may be [found here][cassupport].

[cascodecov]: https://codecov.io/gh/apereo/cas
[devlocity]: https://develocity.apereo.org
[maintenance]: https://apereo.github.io/cas/developer/Maintenance-Policy.html
[releaseschedule]: https://github.com/apereo/cas/milestones
[wiki]: https://apereo.github.io/cas
[githubreleases]: https://github.com/apereo/cas/releases
[gettingstarted]: https://apereo.github.io/cas/development/planning/Getting-Started.html
[overlay]: https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html
[contribute]: https://apereo.github.io/cas/developer/Contributor-Guidelines.html
[cassonatype]: https://oss.sonatype.org/content/repositories/snapshots/org/apereo/cas/
[casmavencentral]: https://search.maven.org/search?q=g:org.apereo.cas
[releasenotes]: https://github.com/apereo/cas/releases
[cassupport]: https://apereo.github.io/cas/Support.html
[casslack]: https://apereo.slack.com/
[blog]: https://apereo.github.io/
[casbuildprocess]: https://apereo.github.io/cas/developer/Build-Process.html
[githubcontributors]: https://github.com/apereo/cas/graphs/contributors
[casjavadocs]: https://www.javadoc.io/doc/org.apereo.cas
[apereocas]: https://www.apereo.org/projects/cas
[springboot]: https://projects.spring.io/spring-boot/
[springcloud]: https://projects.spring.io/spring-cloud/
