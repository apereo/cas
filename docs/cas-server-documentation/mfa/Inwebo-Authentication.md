---
layout: default
title: CAS - Inwebo Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Inwebo Authentication

You can secure your CAS server with a second factor provided by [Inwebo](https://www.inwebo.com).

Apart from this CAS integration, notice that this identity 
provider can provide more general authentication solutions.

Support is enabled by including the following module in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-inwebo-mfa" %}

The integration adds support for both push mobile/desktop and browser authentications.

## Configuration

{% include casproperties.html properties="cas.authn.mfa.inwebo" %}
