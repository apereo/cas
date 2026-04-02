---
layout: default
title: CAS - OpenID Connect Federation
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication

The CAS server supports the [OpenID Federation protocol](https://openid.net/specs/openid-federation-1_0.html).

It can act as:
- a Trust Anchor
- an Intermediate
- an OP.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc-federation" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.oidc.federation" %}
