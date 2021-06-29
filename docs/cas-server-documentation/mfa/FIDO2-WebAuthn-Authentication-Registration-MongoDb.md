---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# MongoDb FIDO2 WebAuthn Multifactor Registration

Device registrations may be kept inside a MongoDb instance by including the following module in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn-mongo" %}

{% include casproperties.html properties="cas.authn.mfa.web-authn.mongo" %}
