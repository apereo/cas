<p align="center">
  <img src="https://github.com/user-attachments/assets/ce80e360-4df8-4a4b-897e-6ab547e8f906" alt="Apereo CAS" />
</p>

<h1 align="center">Apereo CAS</h1>

<p align="center">
  <strong>Open-source single sign-on and identity provider for the web.</strong><br/>
  One login for all your applications, over CAS, SAML2, OAuth2 and OpenID Connect, with multifactor authentication built in.
</p>

<p align="center">
  <a href="https://github.com/apereo/cas/releases"><img src="https://img.shields.io/github/release/apereo/cas.svg?style=flat-square&logo=github&label=release" alt="Latest release" /></a>
  <a href="https://central.sonatype.com/namespace/org.apereo.cas"><img src="https://img.shields.io/maven-central/v/org.apereo.cas/cas-server-webapp?style=flat-square&logo=apachemaven&label=maven%20central" alt="Maven Central" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/apereo/cas?style=flat-square" alt="Apache 2.0 license" /></a>
  <a href="https://apereo.slack.com/"><img src="https://img.shields.io/badge/slack-join%20chat-4A154B?style=flat-square&logo=slack" alt="Slack" /></a>
  <a href="https://codecov.io/gh/apereo/cas"><img src="https://img.shields.io/codecov/c/github/apereo/cas/master?style=flat-square&logo=codecov" alt="Code coverage" /></a>
  <a href="https://community.develocity.cloud/scans?search.rootProjectNames=cas-server"><img src="https://img.shields.io/badge/revved%20up%20by-Develocity-06A0CE?style=flat-square&logo=gradle" alt="Revved up by Develocity" /></a>
</p>

<p align="center">
  <a href="https://apereo.github.io/cas/development/planning/Quick-Start.html"><strong>Quick Start</strong></a> ·
  <a href="https://apereo.github.io/cas/">Documentation</a> ·
  <a href="https://github.com/apereo/cas/releases">Releases</a> ·
  <a href="https://apereo.github.io/cas/Support.html">Support</a> ·
  <a href="https://apereo.github.io/cas/developer/Contributor-Guidelines.html">Contribute</a>
</p>

---

CAS is an enterprise, multilingual identity provider and single sign-on server. It started as the
[CAS protocol](https://apereo.github.io/cas/development/protocol/CAS-Protocol.html), an open and well-documented
authentication protocol, and this repository holds its primary implementation: a Java server built on
[Spring Boot](https://spring.io/projects/spring-boot) and [Spring Cloud](https://spring.io/projects/spring-cloud),
with support for a plethora of authentication protocols and features. CAS is free and open source, managed by
the [Apereo Foundation](https://www.apereo.org/programs/software/cas) and licensed under [Apache 2.0](LICENSE).

## Try It in a Minute

With Docker installed, start the official image, using a CAS version
[tagged on Docker Hub](https://hub.docker.com/r/apereo/cas/tags) in place of `<version>`:

```bash
docker run --rm -p 8080:8080 \
  -e SERVER_SSL_ENABLED=false -e SERVER_PORT=8080 \
  apereo/cas:<version>
```

Open http://localhost:8080/cas/login and log in as `casuser` with the password `Mellon`.

When you are ready for your own server, the [Quick Start](https://apereo.github.io/cas/development/planning/Quick-Start.html)
takes you from here to a configured [WAR overlay][overlay] with a registered application in eight short steps, and
[Getting Started][gettingstarted] explains how to plan a deployment. You do not need to clone this repository to run
CAS; that is only required to contribute to the project.

## What CAS Does

| Area                           | Highlights                                                                                                                                                                       |
|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Protocols**                  | CAS v1, v2 and v3, SAML v1 and v2, OAuth 2.0, OpenID Connect, OpenID for Verifiable Credentials, WS-Federation Passive Requester                                                 |
| **Authentication**             | LDAP and Active Directory, RDBMS, X.509, SPNEGO, JAAS, RADIUS, JWT, MongoDB, Apache Cassandra, BASIC, Remote, Trusted, passkeys and passwordless login, and more                 |
| **Delegated authentication**   | External SAML2, OpenID Connect, OAuth, CAS and WS-Federation identity providers, including social logins                                                                         |
| **Multifactor authentication** | Duo Security, WebAuthn FIDO2, Google Authenticator, YubiKey, Simple MFA, RADIUS and more                                                                                         |
| **Authorization**              | Heimdall, OpenFGA, Open Policy Agent, ABAC, time and date rules, REST, Internet2 Grouper and more                                                                                |
| **High availability**          | Ticket registries on Hazelcast, Redis, JPA, MongoDB, DynamoDb, Memcached, Apache Ignite and more                                                                                 |
| **Application registration**   | Service registries on JSON, YAML, LDAP, JPA, MongoDB, DynamoDb, Redis, Google Cloud and more                                                                                     |
| **User experience**            | Global and per-application themes, password management and policy enforcement, attribute release consent, notifications by email and SMS (Twilio, Mailgun, SendGrid, Amazon SES) |
| **Integrations**               | Apache Syncope, SCIM, Shibboleth IdP, Keycloak, Okta, Swagger and more                                                                                                           |
| **Operations**                 | Admin dashboards, monitoring, metrics, logging and audits; runs on embedded Apache Tomcat or Jetty, in Docker containers and on Kubernetes                                       |

## Documentation

| Version       | Status                               | Docs                                             |
|---------------|--------------------------------------|--------------------------------------------------|
| `development` | Ongoing work toward the next release | [Read](https://apereo.github.io/cas/development) |
| `8.0.x`       | Maintenance Release                  | [Read](https://apereo.github.io/cas/8.0.x)       |
| `7.3.x`       | Maintenance Release                  | [Read](https://apereo.github.io/cas/7.3.x)       |

Release dates and end-of-life schedules are in the [Maintenance Policy][maintenance] and the
[release schedule][releaseschedule]. The [Apereo blog](https://apereo.github.io/) covers new releases, how-tos and
deployment stories.

## Contribute

If you have identified an enhancement or a bug, please submit a pull request for it. There is no need to open a separate
issue first: the pull request *is* the issue, and it is tracked and tagged as such. The [contributor guide][contribute]
explains how the project works, and the [build guide][casbuildprocess] shows how to build CAS from source with JDK 25.

<a href="https://github.com/apereo/cas/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=apereo/cas" alt="Contributors to Apereo CAS" />
</a>

## Support

Apereo CAS is 100% free open source software. The community has access to every release at no cost, and the time and
effort to develop and maintain it are given by [volunteers and contributors](https://github.com/apereo/cas/graphs/contributors).
If you or your employer benefit from CAS, please consider becoming a [Friend of Apereo](https://www.apereo.org/join-us/friends-apereo).

Questions are welcome on the [mailing lists and Slack][cassupport], where you will also find commercial support options.
To report a security issue, please follow the [security policy](SECURITY.md).

[maintenance]: https://apereo.github.io/cas/developer/Maintenance-Policy.html
[releaseschedule]: https://github.com/apereo/cas/milestones
[gettingstarted]: https://apereo.github.io/cas/development/planning/Getting-Started.html
[overlay]: https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html
[contribute]: https://apereo.github.io/cas/developer/Contributor-Guidelines.html
[cassupport]: https://apereo.github.io/cas/Support.html
[casbuildprocess]: https://apereo.github.io/cas/developer/Build-Process.html
