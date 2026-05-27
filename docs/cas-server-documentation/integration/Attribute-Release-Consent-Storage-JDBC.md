---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# JDBC - Attribute Consent Storage

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-consent-jdbc" %}

## Configuration

{% include_cached casproperties.html properties="cas.consent.jpa" %}

## Multitenancy

Configuration settings for attribute consent storage can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Overview.html) for more information.
