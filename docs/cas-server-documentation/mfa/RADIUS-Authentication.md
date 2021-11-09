---
layout: default
title: CAS - RADIUS Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# RADIUS Authentication

RADIUS support is enabled by only including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-radius" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.radius" %}

# RSA RADIUS MFA

RSA RADIUS OTP support for MFA is enabled by only including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-radius-mfa" %}

{% include_cached casproperties.html properties="cas.authn.mfa.radius" %}
