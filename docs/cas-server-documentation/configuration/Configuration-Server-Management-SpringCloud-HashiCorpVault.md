---
layout: default
title: CAS - Spring Cloud Configuration Server - Spring Cloud HashiCorp Vault
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud HashiCorp Vault

Spring Cloud Configuration Server is able to use [Vault](https://www.vaultproject.io/) to locate properties and settings. 
To learn more about configuration security, [please review this guide](Configuration-Properties-Security.html).

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.vault" %}

<div class="alert alert-info mt-3">:information_source: <strong>Usage</strong><p>The configuration modules provided here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="health" healthIndicators="refresh,vaultHealthIndicator" %}

## Token Authentication

Tokens are the core method for authentication within Vault. Token
authentication requires a static token to be provided.

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.vault.token"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}

## AppRole Authentication

AppRole authentication lets CAS log into Vault with a role id and a secret id, which suits machines and services.
The role id identifies the role, and the secret id can be pulled from Vault or provided to CAS when it starts.

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.vault.app-role"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}

## Kubernetes Authentication

Kubernetes authentication mechanism allows CAS to authenticate with Vault
using a Kubernetes Service Account Token. The authentication is role
based and the role is bound to a service account name and a namespace.

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.vault.kubernetes"
thirdPartyExactMatch="spring.cloud.vault.authentication"
%}

## Key-Value Backend

Settings are read from the key-value secrets engine, in either of its versions; the generic backend of earlier
versions is no longer offered.

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.vault.kv" %}

