---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud HashiCorp Vault

Spring Cloud Configuration Server is able to use [Vault](https://www.vaultproject.io/) to locate properties and settings. 
To learn more about configuration security, [please review this guide](Configuration-Properties-Security.html).

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.vault" %}

<div class="alert alert-info mt-3"><strong>Usage</strong><p>The configuration modules provide here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

## Token Authentication

Tokens are the core method for authentication within Vault. Token
authentication requires a static token to be provided.

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.vault.token"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}

## AppID Authentication

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

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.vault.app-id"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}


## Kubernetes Authentication

Kubernetes authentication mechanism allows to authenticate with Vault
using a Kubernetes Service Account Token. The authentication is role
based and the role is bound to a service account name and a namespace.

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.vault.kubernetes"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}

## Generic Backend v1

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.vault.generic" %}

## KV Backend v2

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.vault.kv" %}

