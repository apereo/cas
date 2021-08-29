---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JPA FIDO2 WebAuthn Multifactor Registration

Device registrations may be kept inside a relational database 
instance by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn-jpa" %}

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.jpa" %}
