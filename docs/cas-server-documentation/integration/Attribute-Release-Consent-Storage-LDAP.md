---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# LDAP - Attribute Consent Storage

Consent decisions can be stored on LDAP user objects. The decisions
are serialized into JSON and stored one-by-one in a multi-valued string attribute.

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-consent-ldap" %}

## Configuration

{% include_cached casproperties.html properties="cas.consent.ldap" %}

