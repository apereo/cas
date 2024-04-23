---
layout: default
title: CAS - Simple Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Simple Multifactor Authentication - Token Management

Token management and issuance can be handled by CAS directly or can be outsources to external systems and services.

By default, tokens issued by CAS are tracked using the [ticket registry](../ticketing/Configuring-Ticketing-Components.html)
and are assigned a configurable expiration policy controlled via CAS settings. In this option, CAS itself is in charge of
managing and validating tokens using pre-configured policies and components.

{% include_cached casproperties.html properties="cas.authn.mfa.simple.token.core" %}
