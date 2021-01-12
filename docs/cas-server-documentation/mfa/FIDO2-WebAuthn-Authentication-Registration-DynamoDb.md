---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# DynamoDb FIDO2 WebAuthn Multifactor Registration

Device registrations may be kept inside a DynamoDb instance by including the following module in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn-dynamodb" %}

{% include casproperties.html
modules="cas-server-support-webauthn-dynamodb" %}
