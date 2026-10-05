---
layout: default
title: CAS - DynamoDb YubiKey Registration
category: Multifactor Authentication
---

{% include variables.html %}

# DynamoDb YubiKey Registration

Support is enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-yubikey-dynamodb" %}

{% include_cached casproperties.html properties="cas.authn.mfa.yubikey.dynamo-db" %}
