---
layout: default
title: CAS - Apache Syncope Provisioning
category: Integration
---

{% include variables.html %}

# Apache Syncope - Principal Provisioning

CAS may be allowed to provision the authenticated principal via Apache Syncope to a provisioning/identity/entity 
engine which would then dynamically synchronize user profiles to target systems. Successful login attempts would allow CAS
to pass back the authenticated user profile to Apache Syncope, which would then be tasked to create and provision the user
to appropriate target systems. 

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-syncope-authentication" %}

{% include_cached casproperties.html properties="cas.authn.syncope.provisioning" %}
