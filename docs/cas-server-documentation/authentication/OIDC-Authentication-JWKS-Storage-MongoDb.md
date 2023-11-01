---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication JWKS Storage - MongoDb

Keystore generation can be outsourced to an external MongoDb instance.

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc-mongo" %}

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.mongo" %}
