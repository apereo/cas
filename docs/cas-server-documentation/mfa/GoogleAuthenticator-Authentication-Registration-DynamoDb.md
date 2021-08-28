---
layout: default
title: CAS - Google Authenticator Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# DynamoDb Google Authenticator Registration

Registration records and tokens may be kept inside a DynamoDb instance via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gauth-dynamodb" %}

{% include_cached casproperties.html properties="cas.authn.mfa.gauth.dynamo-db" %}
