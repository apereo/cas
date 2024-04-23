---
layout: default
title: CAS - YubiKey Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Redis YubiKey Registration

Support is enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-yubikey-redis" %}

{% include_cached casproperties.html properties="cas.authn.mfa.yubikey.redis" %}
