---
layout: default
title: CAS - Apache Syncope Provisioning
category: Integration
---

{% include variables.html %}

# Okta - Principal Provisioning

CAS may be allowed to provision the authenticated principal to Okta. Successful login attempts would allow CAS
to pass back the authenticated user profile to Okta, mapping the authenticated user attributes to the Okta user profile
and either create or update the Okta account.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-okta-authentication" %}

{% include_cached casproperties.html properties="cas.authn.okta.provisioning" %}
