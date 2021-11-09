---
layout: default
title: CAS - Google Authenticator Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# MongoDb Google Authenticator Registration

Registration records and tokens may be kept inside a MongoDb instance, via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gauth-mongo" %}

{% include_cached casproperties.html properties="cas.authn.mfa.gauth.mongo" %}
