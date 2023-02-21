---
layout: default
title: CAS - Inwebo Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Inwebo Multifactor Authentication

You can secure your CAS server with a second factor provided by [Inwebo](https://www.inwebo.com).

A complete documentation of this integration can be found [on the Inwebo documentation website](https://docs.inwebo.com/documentation/cas-apereo-inwebo-integration).

Apart from this CAS integration, notice that this identity provider can provide more general authentication solutions.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-inwebo-mfa" %}

The integration adds support for both push mobile/desktop and browser authentications (Virtual Authenticator or mAccess WEB).

## Configuration

{% include_cached casproperties.html properties="cas.authn.mfa.inwebo" %}
