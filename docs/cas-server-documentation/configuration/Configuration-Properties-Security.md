---
layout: default
title: CAS - Configuration Security
description: "Secure CAS configuration: encrypt and decrypt sensitive settings with Jasypt, and fetch secrets from Vault, cloud secret managers or Docker secrets."
category: Configuration
---

{% include variables.html %}

# Configuration Security

This document describes how to retrieve and secure CAS configuration and properties
using the techniques specified below. Please note that configuration security and encryption/decryption
strategies not only may apply to individual settings but also may be applicable to file/resource contents that
may have been defined using a specific setting.

| Policy       | Resource                                                             |
|--------------|----------------------------------------------------------------------|
| CAS          | [See this page](Configuration-Properties-Security-CAS.html).         |
| Spring Cloud | [See this page](Configuration-Properties-Security-SpringCloud.html). |
| Vault        | [See this page](Configuration-Properties-Security-Vault.html).       |
