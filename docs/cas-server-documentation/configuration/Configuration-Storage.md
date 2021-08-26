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

This section outlines strategies that can be used to store CAS configuration and settings.

## Standalone

This is the default configuration mode which indicates that CAS does NOT require connections
to an external configuration server and will run in an embedded standalone mode.
  
{% include casproperties.html properties="cas.standalone." excludes="configuration-security" %}

### By Directory

CAS by default will attempt to locate settings and properties inside a given directory and otherwise falls back to using:

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

### By File

You can also use a dedicated configuration file to directly feed a collection of properties
to CAS in form of a file or classpath resource. This is specially useful in cases 
where a bare CAS server is deployed in the cloud without
the extra ceremony of a configuration server or an external directory for 
that matter and the deployer wishes to avoid overriding embedded configuration files.

## Spring Cloud

The following settings are to be loaded by the CAS configuration runtime, which bootstraps
the entire CAS running context. They are to be put inside the `src/main/resources/bootstrap.properties`
of the configuration server itself. See [this guide](Configuration-Server-Management.html) for more info.

The configuration server backed by Spring Cloud supports the following profiles.

### Native

Load settings from external properties/yaml configuration files.

{% include casproperties.html
thirdPartyStartsWith="spring.cloud.config.server.native"
thirdPartyExactMatch="spring.profiles.active"
%}


### Git Repository

Allow the CAS Spring Cloud configuration server to load settings from an internal/external Git repository.
This then allows CAS to become a client of the configuration server, consuming settings over HTTP where needed.

{% include casproperties.html
thirdPartyStartsWith="spring.cloud.config.server.git"
thirdPartyExactMatch="spring.profiles.active"
%}

The above configuration also applies to online git-based repositories such as Github, BitBucket, etc.

### Consul

Allow the CAS Spring Cloud configuration server to load settings from [HashiCorp's Consul](../installation/Service-Discovery-Guide-Consul.html).

{% include casproperties.html
thirdPartyStartsWith="spring.cloud.consul.config"
%}

### Vault

Allow the CAS Spring Cloud configuration server 
to load settings from [HashiCorp's Vault](Configuration-Properties-Security.html).

{% include casproperties.html
thirdPartyStartsWith="spring.cloud.vault"
%}

#### Token Authentication

Tokens are the core method for authentication within Vault. Token 
authentication requires a static token to be provided.

{% include casproperties.html
thirdPartyStartsWith="spring.cloud.vault.token"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}

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

Using MAC address:

```bash
export $MAC_ADDRESS=`echo -n ABCDEFGH | sha256sum`
```

{% include casproperties.html
thirdPartyStartsWith="spring.cloud.vault.app-id"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}


#### Kubernetes Authentication

Kubernetes authentication mechanism allows to authenticate with Vault 
using a Kubernetes Service Account Token. The authentication is role 
based and the role is bound to a service account name and a namespace.

{% include casproperties.html 
thirdPartyStartsWith="spring.cloud.vault.kubernetes"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}

#### Generic Backend v1

{% include casproperties.html thirdPartyStartsWith="spring.cloud.vault.generic" %}

#### KV Backend v2

{% include casproperties.html thirdPartyStartsWith="spring.cloud.vault.kv" %}

### MongoDb

Allow the CAS Spring Cloud configuration server to load settings from a MongoDb instance.

{% include casproperties.html properties="cas.spring.cloud.mongo" %}

### Azure KeyVault Secrets

Allow the CAS Spring Cloud configuration server to load settings from Microsoft Azure's KeyVault instance.

{% include casproperties.html thirdPartyStartsWith="azure.keyvault" %}

### ZooKeeper

Allow the CAS Spring Cloud configuration server to load settings from an Apache ZooKeeper instance.

{% include casproperties.html thirdPartyStartsWith="spring.cloud.zookeeper" %}

### Amazon Secrets Manager

{% include casproperties.html properties="cas.spring.cloud.aws.secrets-manager" %}

### Amazon Parameter Store

{% include casproperties.html properties="cas.spring.cloud.aws.ssm" %}

### Amazon S3

{% include casproperties.html properties="cas.spring.cloud.aws.s3" %}

### DynamoDb

{% include casproperties.html properties="cas.spring.cloud.dynamo-db" %}

### JDBC

Allow the CAS Spring Cloud configuration server to load settings from a RDBMS instance.

{% include casproperties.html properties="cas.spring.cloud.jdbc" %}

### REST

Allow the CAS Spring Cloud configuration server to load settings from a REST API.

{% include casproperties.html properties="cas.spring.cloud.rest" %}

## Configuration Security

To learn more about how sensitive CAS settings can be
secured, [please review this guide](Configuration-Properties-Security.html).

