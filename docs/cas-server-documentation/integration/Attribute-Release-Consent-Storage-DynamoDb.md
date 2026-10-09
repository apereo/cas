---
layout: default
title: CAS - DynamoDb - Attribute Consent Storage
category: Attributes
---

{% include variables.html %}

# DynamoDb - Attribute Consent Storage

Keep the attribute consent decisions users make in an Amazon DynamoDB table.

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-consent-dynamodb" %}

## Configuration

{% include_cached casproperties.html properties="cas.consent.dynamo-db" %}
