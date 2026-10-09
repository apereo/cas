---
layout: default
title: CAS - DynamoDb FIDO2 WebAuthn Multifactor Registration
category: Multifactor Authentication
---

{% include variables.html %}

# DynamoDb FIDO2 WebAuthn Multifactor Registration

Keep FIDO2 WebAuthn device registrations in an Amazon DynamoDB table.

Device registrations may be kept inside a DynamoDb instance by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn-dynamodb" %}

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.dynamo-db" %}
