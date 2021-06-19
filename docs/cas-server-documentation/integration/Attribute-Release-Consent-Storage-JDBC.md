---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# JDBC - Attribute Consent Storage

Support is enabled by including the following module in the WAR Overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-consent-jdbc" %}

## Configuration

{% include casproperties.html properties="cas.consent.jpa" %}
