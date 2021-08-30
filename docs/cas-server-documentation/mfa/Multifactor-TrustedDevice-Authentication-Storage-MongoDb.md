---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# MongoDb Device Storage - Multifactor Authentication Trusted Device/Browser

User decisions may also be kept inside a MongoDb instance.

Support is provided via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-trusted-mfa-mongo" %}

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.mongo" %}
