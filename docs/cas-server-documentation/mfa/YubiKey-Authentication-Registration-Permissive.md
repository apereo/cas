---
layout: default
title: CAS - YubiKey Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Permissive YubiKey Registration

Registration records may be specified statically via CAS settings in form of a map that links registered usernames 
with the public id of the YubiKey device. 

{% include_cached casproperties.html properties="cas.authn.mfa.yubikey.allowed-devices" %}
