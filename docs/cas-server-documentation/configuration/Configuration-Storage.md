---
layout: default
title: Configuration Storage
category: Configuration
---

{% include variables.html %}
       
# CAS Configuration Storage

Various properties can be specified in CAS [either inside configuration files or as command
line switches](Configuration-Management.html#overview). This section provides a list common CAS properties and
references to the underlying modules that consume them.

<div class="alert alert-warning"><strong>Be Selective</strong><p>
This section is meant as a guide only. Do <strong>NOT</strong> copy/paste the entire 
collection of settings into your CAS configuration; rather pick only the properties 
that you need. Do NOT enable settings unless you are certain of their purpose 
and do NOT copy settings into your configuration only to keep 
them as <i>reference</i>. All these ideas lead to upgrade headaches, 
maintenance nightmares and premature aging.</p></div>

The following list of properties are controlled by and provided to CAS. Each block, for most use cases, corresponds
to a specific CAS module that is expected to be included in the final CAS distribution prepared during the build
and deployment process.

<div class="alert alert-info"><strong>YAGNI</strong><p>Note that for nearly ALL use cases,
 declaring and configuring properties listed below is sufficient. You should NOT have to
explicitly massage a CAS XML configuration file to design an authentication handler,
create attribute release policies, etc. CAS at runtime will auto-configure all required changes for you.</p></div>

## Naming Convention

Property names can be specified in very relaxed terms. For
instance `cas.someProperty`, `cas.some-property`, `cas.some_property` are all valid 
names. While all forms are accepted by CAS, there are
certain components (in CAS and other frameworks used) whose activation at runtime is conditional on a
property value, where this property is required to have been specified in CAS configuration using kebab case. This
is both true for properties that are owned by CAS as well as those that might be presented to the system via
an external library or framework such as Spring Boot, etc.

> When possible, properties should be stored in lower-case kebab format, such as cas.property-name=value.

## Validation

Configuration properties are automatically validated on CAS startup to report issues with configuration binding,
specially if defined CAS settings cannot be recognized or validated by the configuration schema. The validation process
is on by default and can be skipped on startup using a special *system property* `SKIP_CONFIG_VALIDATION`
that should be set to `true`.

Additional validation processes are also handled via [Configuration Metadata](Configuration-Metadata-Repository.html)
and property migrations applied automatically on startup by Spring Boot and family.

## Configuration Storage

This section outlines strategies that can be used to store CAS configuration and settings.

### Standalone

This is the default configuration mode which indicates that CAS does NOT require connections
to an external configuration server and will run in an embedded standalone mode.

#### By Directory

CAS by default will attempt to locate settings and properties inside a given directory indicated
under the setting name `cas.standalone.configuration-directory` and otherwise falls back to using:

1. `/etc/cas/config`
2. `/opt/cas/config`
3. `/var/cas/config`

CAS has the ability to also load a Groovy file for loading settings. The file 
is expected to be found at the above matching
directory and should be named `${cas-application-name}.groovy`, such as `cas.groovy`. The 
script is able to combine conditional settings for active profiles and common settings 
that are applicable to all environments and profiles into one location with a 
structure that is similar to the below example:

```groovy
// Settings may be filtered by individual profiles
profiles {
    standalone {
        cas.some.setting="value"
    }
}

// This applies to all profiles and environments
cas.common.setting="value"
``` 

#### By File

There also exists a `cas.standalone.configuration-file` which can be 
used to directly feed a collection of properties
to CAS in form of a file or classpath resource. This is specially useful in cases 
where a bare CAS server is deployed in the cloud without
the extra ceremony of a configuration server or an external directory for 
that matter and the deployer wishes to avoid overriding embedded configuration files.

### Spring Cloud

The following settings are to be loaded by the CAS configuration runtime, which bootstraps
the entire CAS running context. They are to be put inside the `src/main/resources/bootstrap.properties`
of the configuration server itself. See [this guide](Configuration-Server-Management.html) for more info.

The configuration server backed by Spring Cloud supports the following profiles.

### Native

Load settings from external properties/yaml configuration files.

```properties
# spring.profiles.active=native

# The configuration directory where CAS should monitor to locate settings.
# spring.cloud.config.server.native.search-locations=file:///etc/cas/config
```

### Git Repository

Allow the CAS Spring Cloud configuration server to load settings from an internal/external Git repository.
This then allows CAS to become a client of the configuration server, consuming settings over HTTP where needed.

```properties
# spring.profiles.active=default

# The location of the git repository that contains CAS settings.
# The location can point to an HTTP/SSH/directory.
# spring.cloud.config.server.git.uri=https://github.com/repoName/config
# spring.cloud.config.server.git.uri=file://${user.home}/config

# The credentials used to authenticate git requests, specially
# when using HTTPS. If connecting to the repository via SSH, remember
# to register your public keys with an SSH agent just as your normal would have
# with any other public repository.
# spring.cloud.config.server.git.username=
# spring.cloud.config.server.git.password=
```

The above configuration also applies to online git-based repositories such as Github, BitBucket, etc.

### Consul

Allow the CAS Spring Cloud configuration server to load settings from [HashiCorp's Consul](../installation/Service-Discovery-Guide-Consul.html).

```properties
# spring.cloud.consul.config.enabled=true
# spring.cloud.consul.config.prefix=configuration
# spring.cloud.consul.config.default-context=apps
# spring.cloud.consul.config.profile-separator=::

# spring.cloud.consul.config.watch.delay=1000
# spring.cloud.consul.config.watch.enabled=false
```

### Vault

Allow the CAS Spring Cloud configuration server to load settings from [HashiCorp's Vault](Configuration-Properties-Security.html).

```properties
# spring.cloud.vault.host=127.0.0.1
# spring.cloud.vault.port=8200
# spring.cloud.vault.connection-timeout=3000
# spring.cloud.vault.read-timeout=5000
# spring.cloud.vault.enabled=true
# spring.cloud.vault.fail-fast=true
# spring.cloud.vault.scheme=http
```

#### Token Authentication

Tokens are the core method for authentication within Vault. Token 
authentication requires a static token to be provided.

```properties
# spring.cloud.vault.authentication=TOKEN
# spring.cloud.vault.token=1305dd6a-a754-f145-3563-2fa90b0773b7
```

#### AppID Authentication

Vault supports AppId authentication that consists of two hard to guess 
tokens. The AppId defaults to `spring.application.name` that is statically 
configured. The second token is the UserId which is a part determined by the 
application, usually related to the runtime environment. Spring Cloud Vault 
Config supports IP address, Mac address and static
UserId’s (e.g. supplied via System properties). The IP and Mac address are 
represented as Hex-encoded SHA256 hash.

Using IP addresses:

```bash
export IP_ADDRESS=`echo -n 192.168.99.1 | sha256sum`
```

```properties
# spring.cloud.vault.authentication=APPID
# spring.cloud.vault.app-id.user-id=$IP_ADDRESS
```

Using MAC address:

```bash
export $MAC_ADDRESS=`echo -n ABCDEFGH | sha256sum`
```

```properties
# spring.cloud.vault.authentication=APPID
# spring.cloud.vault.app-id.user-id=$MAC_ADDRESS
# spring.cloud.vault.app-id.network-interface=eth0
```

#### Kubernetes Authentication

Kubernetes authentication mechanism allows to authenticate with Vault using a Kubernetes Service Account Token. The authentication is role based and the role is bound to a service account name and a namespace.

```properties
# spring.cloud.vault.authentication=KUBERNETES
# spring.cloud.vault.kubernetes.role=my-dev-role
# spring.cloud.vault.kubernetes.service-account-token-file: /var/run/secrets/kubernetes.io/serviceaccount/token
```

#### Generic Backend v1

```properties
# spring.cloud.vault.generic.enabled=true
# spring.cloud.vault.generic.backend=secret
```

#### KV Backend v2

```properties
# spring.cloud.vault.kv.enabled=true
# spring.cloud.vault.kv.backend=secret
```

### MongoDb

Allow the CAS Spring Cloud configuration server to load settings from a MongoDb instance.

```properties
# cas.spring.cloud.mongo.uri=mongodb://casuser:Mellon@ds135522.mlab.com:35522/jasigcas
```

### Azure KeyVault Secrets

Allow the CAS Spring Cloud configuration server to load settings from Microsoft Azure's KeyVault instance.

```properties
# azure.keyvault.enabled=true
# azure.keyvault.uri=put-your-azure-keyvault-uri-here
# azure.keyvault.client-id=put-your-azure-client-id-here
# azure.keyvault.client-key=put-your-azure-client-key-here
# azure.keyvault.token-acquire-timeout-seconds=60
```

### ZooKeeper

Allow the CAS Spring Cloud configuration server to load settings from an Apache ZooKeeper instance.

```properties
# spring.cloud.zookeeper.connect-string=localhost:2181
# spring.cloud.zookeeper.enabled=true
# spring.cloud.zookeeper.config.enabled=true
# spring.cloud.zookeeper.max-retries=10
# spring.cloud.zookeeper.config.root=cas/config
```

### Amazon Secrets Manager

{% include {{ version }}/aws-configuration.md configKey="cas.spring.cloud.aws.secrets-manager" %}

### Amazon Parameter Store

{% include {{ version }}/aws-configuration.md configKey="cas.spring.cloud.aws.ssm" %}


### Amazon S3

{% include {{ version }}/aws-configuration.md configKey="cas.spring.cloud.aws.s3" %}

```properties
# ${configuration-key}.bucket-name=cas-properties
```

### DynamoDb

{% include {{ version }}/dynamodb-configuration.md configKey="cas.spring.cloud.dynamo-db" %}

### JDBC

Allow the CAS Spring Cloud configuration server to load settings from a RDBMS instance.

{% include {{ version }}/rdbms-configuration.md configKey="cas.spring.cloud.jdbc" %}

```properties
# cas.spring.cloud.jdbc.sql=SELECT id, name, value FROM CAS_SETTINGS_TABLE
```

### REST

Allow the CAS Spring Cloud configuration server to load settings from a REST API.

{% include {{ version }}/rest-configuration.md configKey="cas.spring.cloud.rest" %}

## Configuration Security

To learn more about how sensitive CAS settings can be
secured, [please review this guide](Configuration-Properties-Security.html).

### Standalone

```properties
# cas.standalone.configuration-security.alg=PBEWithMD5AndTripleDES
# cas.standalone.configuration-security.provider=BC
# cas.standalone.configuration-security.iterations=
# cas.standalone.configuration-security.psw=
```

The above settings may be passed to CAS using any of the [strategies outline here](Configuration-Management.html#overview),
though it might be more secure to pass them to CAS as either command-line or system properties.

### Spring Cloud

Encrypt and decrypt configuration via Spring Cloud, if the Spring Cloud configuration server is used.

```properties
# spring.cloud.config.server.encrypt.enabled=true

# encrypt.key-store.location=file:///etc/cas/casconfigserver.jks
# encrypt.key-store.password=keystorePassword
# encrypt.key-store.alias=DaKey
# encrypt.key-store.secret=changeme
```
