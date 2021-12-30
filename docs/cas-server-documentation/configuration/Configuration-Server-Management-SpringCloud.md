---
layout: default 
title: CAS - Configuration Server 
category: Configuration
---

{% include variables.html %}

# Configuration Server - Spring Cloud

CAS is able to use an external and central configuration server to obtain state and settings. The configuration server provides a very abstract way for CAS (and
all of its other clients) to obtain settings from a variety of sources, such as file system, `git` or `svn` repositories, MongoDb databases, Vault, etc. The
beauty of this solution is that to the CAS web application server, it matters not where settings come from and it has no knowledge of the underlying property
sources. It talks to the configuration server to locate settings and move on.

<div class="alert alert-info"><strong>Configuration Security</strong><p>This is a very good strategy to ensure configuration settings
are not scattered around various deployment environments leading to a more secure deployment. The configuration server need not be
exposed to the outside world, and it can safely and secure be hidden behind firewalls, etc allowing access to only authorized clients
such as the CAS server web application.</p></div>

A full comprehensive guide is provided by the [Spring Cloud project](https://cloud.spring.io/spring-cloud-config/spring-cloud-config.html).

## Spring Cloud Configuration Server Overlay

The configuration server itself, similar to CAS, can be deployed using the [CAS Initializr](../installation/WAR-Overlay-Initializr.html).

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-webapp-config-server" %}

In addition to the [strategies outlined here](Configuration-Management.html#overview), the configuration server may load CAS settings and properties via the
following order and mechanics:

1. Profile-specific application properties outside of your packaged web application (`application-{profile}.properties|yml`)
2. Profile-specific application properties packaged inside your jar (`application-{profile}.properties|yml`)
3. Application properties outside of your packaged jar (`application.properties|yml`).
4. Application properties packaged inside your jar (`application.properties|yml`).

The configuration and behavior of the configuration server is also controlled by its own
`src/main/resources/bootstrap.properties` file. By default, it runs under port `8888` at `/casconfigserver` inside an embedded Apache Tomcat server whose
endpoints are protected with basic authentication where the default credentials are `casuser` and an auto-generated password defined
in `src/main/resources/application.properties`.

Furthermore, by default it runs under a `native` profile described below.

The following endpoints are secured and exposed by the configuration server:

| Parameter               | Description                                                                          |
|-------------------------|--------------------------------------------------------------------------------------|
| `/encrypt`              | Accepts a `POST` to encrypt CAS configuration settings.                              |
| `/decrypt`              | Accepts a `POST` to decrypt CAS configuration settings.                              |
| `/actuator/refresh`     | Accepts a `POST` and attempts to refresh the internal state of configuration server. |
| `/actuator/env`         | Accepts a `GET` and describes all configuration sources of the configuration server. |
| `/actuator/cas/default` | Describes what the configuration server knows about the `default` settings profile.  |
| `/actuator/cas/native`  | Describes what the configuration server knows about the `native` settings profile.   |

Once you have the configuration server deployed and assuming the credentials used to secure the configuration server match the example below, you can observe
the collection of settings via:

```bash
curl -u casuser:Mellon https://config.server.url:8888/casconfigserver/cas/native
```

Assuming actuator endpoints are enabled in the configuration, you can also observe the collection of property sources that provide settings to the configuration
server:

```bash
curl -u casuser:Mellon https://config.server.url:8888/casconfigserver/actuator/env
```

<div class="alert alert-info"><strong>Actuator Endpoints</strong><p>
Remember that actuator endpoints typically are prefixed with <code>/actuator</code>.
</p></div>

## Spring Cloud Configuration Server Clients

To let the CAS server web application (or any other client for that matter) talk to the configuration server, the following settings need to be applied to CAS'
own `src/main/resources/bootstrap.properties` file. The properties to configure the CAS server web application as the client of the configuration server must
necessarily be read in before the rest of the application’s configuration is read from the configuration server, during the *bootstrap* phase.

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.config" %}

Remember that configuration server serves property sources from `/{name}/{profile}/{label}` to applications, where the default bindings in the client app are
the following:

```bash
"name" = ${spring.application.name}
"profile" = ${spring.profiles.active}
"label" = "master"
```

All of them can be overridden by setting `spring.cloud.config.*` (where `*` is `name`, `profile` or `label`). The "label" is useful for rolling back to previous
versions of configuration; with the default Config Server implementation it can be a git label, branch name or commit id. Label can also be provided as a
comma-separated list, in which case the items in the list are tried on-by-one until one succeeds. This can be useful when working on a feature branch, for
instance, when you might want to align the config label with your branch, but make it optional (e.g. `spring.cloud.config.label=myfeature,develop`).

To lean more about how CAS allows you to reload configuration changes, please [review this guide](Configuration-Management-Reload.html).

## Spring Cloud Configuration Server Sources

Various configuration profiles exist to determine how configuration server should retrieve properties and settings.

- [Default](Configuration-Server-Management-SpringCloud-Default.html)
- [Native](Configuration-Server-Management-SpringCloud-Native.html)
- [REST](Configuration-Server-Management-SpringCloud-REST.html)
- [Amazon S3](Configuration-Server-Management-SpringCloud-AmazonS3.html)
- [Amazon Secret Manager](Configuration-Server-Management-SpringCloud-AmazonSecretManager.html)
- [Amazon SSM](Configuration-Server-Management-SpringCloud-AmazonSSM.html)
- [Azure KeyVault](Configuration-Server-Management-SpringCloud-AzureKeyVault.html)
- [DynamoDb](Configuration-Server-Management-SpringCloud-DynamoDb.html)
- [HashiCorp Consul](Configuration-Server-Management-SpringCloud-HashiCorpConsul.html)
- [HashiCorp Vault](Configuration-Server-Management-SpringCloud-HashiCorpVault.html)
- [JDBC](Configuration-Server-Management-SpringCloud-JDBC.html)
- [MongoDb](Configuration-Server-Management-SpringCloud-MongoDb.html)
- [ZooKeeper](Configuration-Server-Management-SpringCloud-ZooKeeper.html)

The cloud configuration modules provided above may also be used verbatim inside a CAS server overlay. Remember that the
primary objective for these modules is to retrieve settings and properties from a source. While they are mostly and primarily useful when activated inside the
Spring Cloud Configuration server and can be set to honor profiles and such, they nonetheless may also be used inside a CAS server overlay directly to fetch
settings from a source while running in standalone mode. In such scenarios, all sources of configuration regardless of format or syntax will work alongside each
other to retrieve settings and you can certainly mix and match as you see fit.

### Spring Cloud Configuration Server Composite Sources

In some scenarios you may wish to pull configuration data from multiple environment repositories. To do this just enable multiple profiles in your configuration
server’s application properties or YAML file. If, for example, you want to pull configuration data from a Git repository as well as a SVN repository you would
set the following properties for your configuration server.

```yml
spring:
  profiles:
    active: git, svn
  cloud:
    config:
      server:
        svn:
          uri: file:///path/to/svn/repo
          order: 2
        git:
          uri: file:///path/to/git/repo
          order: 1
```

In addition to each repo specifying a URI, you can also specify an `order` property. The `order` property allows you to specify the priority order for all your
repositories. The lower the numerical value of the order property the higher priority it will have. The priority order of a repository will help resolve any
potential conflicts between repositories that contain values for the same properties.

### Spring Cloud Configuration Server Property Overrides

The configuration server has an "overrides" feature that allows the operator to provide configuration properties to all applications that cannot be accidentally
changed by the application using the normal change events and hooks. To declare overrides add a map of name-value pairs
to `spring.cloud.config.server.overrides`.

For example:

```yml
spring:
  cloud:
    config:
      server:
        overrides:
          foo: bar
```

This will cause the CAS server (as the client of the configuration server) to read `foo=bar` independent of its own configuration.

