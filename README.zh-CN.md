<p align="center">
  <img src="https://github.com/user-attachments/assets/ce80e360-4df8-4a4b-897e-6ab547e8f906" alt="Apereo CAS" />
</p>

<h1 align="center">Apereo CAS</h1>

<p align="center">
  <strong>面向 Web 的开源单点登录与身份提供者。</strong><br/>
  一次登录，即可访问你的所有应用，支持 CAS、SAML2、OAuth2 和 OpenID Connect，并内置多因素认证。
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
  <a href="https://apereo.github.io/cas/development/planning/Quick-Start.html"><strong>快速开始</strong></a> ·
  <a href="https://apereo.github.io/cas/">文档</a> ·
  <a href="https://github.com/apereo/cas/releases">版本发布</a> ·
  <a href="https://apereo.github.io/cas/Support.html">支持</a> ·
  <a href="https://apereo.github.io/cas/developer/Contributor-Guidelines.html">参与贡献</a> ·
  <a href="README.md">English</a>
</p>

> 本文档是 [README.md](README.md) 的简体中文翻译，若两者有出入，以英文原版为准。

---

CAS 是一个企业级、多语言的身份提供者与单点登录服务器。它起源于
[CAS 协议](https://apereo.github.io/cas/development/protocol/CAS-Protocol.html)——一个开放且文档完善的
认证协议，本仓库是其主要实现：一个基于
[Spring Boot](https://spring.io/projects/spring-boot) 和 [Spring Cloud](https://spring.io/projects/spring-cloud)
构建的 Java 服务器，支持众多认证协议与特性。CAS 是自由且开源的软件，由
[Apereo 基金会](https://www.apereo.org/programs/software/cas)管理，并以 [Apache 2.0](LICENSE) 许可证发布。

## 一分钟快速体验

安装 Docker 后，即可启动官方镜像，将 `<version>` 替换为
[Docker Hub 上已标记的](https://hub.docker.com/r/apereo/cas/tags) CAS 版本号：

```bash
docker run --rm -p 8080:8080 \
  -e SERVER_SSL_ENABLED=false -e SERVER_PORT=8080 \
  apereo/cas:<version>
```

打开 http://localhost:8080/cas/login，使用用户名 `casuser`、密码 `Mellon` 登录。

当你准备好搭建自己的服务器时，[快速开始](https://apereo.github.io/cas/development/planning/Quick-Start.html)
会通过八个简短的步骤，带你从这里走到一个配置好的 [WAR overlay][overlay] 并注册一个应用；
[入门指南][gettingstarted] 则介绍如何规划部署。运行 CAS 并不需要克隆本仓库；
只有为项目做贡献时才需要。

## CAS 能做什么

| 领域             | 亮点                                                                                                                                                                       |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **协议**         | CAS v1、v2 和 v3，SAML v1 和 v2，OAuth 2.0，OpenID Connect，OpenID for Verifiable Credentials，WS-Federation Passive Requester                                                 |
| **认证**         | LDAP 与 Active Directory、RDBMS、X.509、SPNEGO、JAAS、RADIUS、JWT、MongoDB、Apache Cassandra、BASIC、Remote、Trusted、通行密钥（passkeys）与无密码登录等                        |
| **委托认证**     | 外部 SAML2、OpenID Connect、OAuth、CAS 和 WS-Federation 身份提供者，包括社交登录                                                                                              |
| **多因素认证**   | Duo Security、WebAuthn FIDO2、Google Authenticator、YubiKey、Simple MFA、RADIUS 等                                                                                            |
| **授权**         | Heimdall、OpenFGA、Open Policy Agent、ABAC、时间与日期规则、REST、Internet2 Grouper 等                                                                                        |
| **高可用**       | 基于 Hazelcast、Redis、JPA、MongoDB、DynamoDb、Memcached、Apache Ignite 等的票据注册表                                                                                        |
| **应用注册**     | 基于 JSON、YAML、LDAP、JPA、MongoDB、DynamoDb、Redis、Google Cloud 等的服务注册表                                                                                             |
| **用户体验**     | 全局与按应用的主题、密码管理与策略强制执行、属性释放同意确认、电子邮件与短信通知（Twilio、Mailgun、SendGrid、Amazon SES）                                                      |
| **集成**         | Apache Syncope、SCIM、Shibboleth IdP、Keycloak、Okta、Swagger 等                                                                                                              |
| **运维**         | 管理仪表盘、监控、指标、日志与审计；可运行于内嵌 Apache Tomcat 或 Jetty、Docker 容器以及 Kubernetes                                                                           |

## 文档

| 版本          | 状态                     | 文档                                             |
|---------------|--------------------------|--------------------------------------------------|
| `development` | 面向下一个版本的持续开发 | [阅读](https://apereo.github.io/cas/development) |
| `8.0.x`       | 维护版本                 | [阅读](https://apereo.github.io/cas/8.0.x)       |
| `7.3.x`       | 维护版本                 | [阅读](https://apereo.github.io/cas/7.3.x)       |

发布日期与生命周期终止（EOL）时间表见[维护策略][maintenance]和
[发布计划][releaseschedule]。[Apereo 博客](https://apereo.github.io/) 涵盖新版本发布、操作指南和
部署案例。

## 参与贡献

如果你发现了可改进之处或 bug，请直接提交 pull request。无需先单独开
issue：pull request *本身*就是 issue，并会按此进行跟踪和打标。[贡献者指南][contribute]
介绍项目的运作方式，[构建指南][casbuildprocess] 说明如何从源码构建 CAS。

<a href="https://github.com/apereo/cas/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=apereo/cas" alt="Contributors to Apereo CAS" />
</a>

## 支持

Apereo CAS 是 100% 免费的开源软件。社区可以免费获取每一个发行版，开发与维护它的时间和
精力来自[志愿者与贡献者](https://github.com/apereo/cas/graphs/contributors)。
如果你或你的雇主从 CAS 中受益，请考虑成为 [Apereo 之友](https://www.apereo.org/join-us/friends-apereo)。

欢迎在[邮件列表和 Slack][cassupport] 上提问，那里也能找到商业支持选项。
报告安全问题请遵循[安全策略](SECURITY.md)。

[maintenance]: https://apereo.github.io/cas/developer/Maintenance-Policy.html
[releaseschedule]: https://github.com/apereo/cas/milestones
[gettingstarted]: https://apereo.github.io/cas/development/planning/Getting-Started.html
[overlay]: https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html
[contribute]: https://apereo.github.io/cas/developer/Contributor-Guidelines.html
[cassupport]: https://apereo.github.io/cas/Support.html
[casbuildprocess]: https://apereo.github.io/cas/developer/Build-Process.html