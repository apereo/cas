---
layout: default
title: CAS - Account Registration Provisioning
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration - Apache Syncope Provisioning

Account registration requests can be submitted to Apache Syncope. Support is enabled by including the 
following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-syncope-authentication" %}

{% include_cached casproperties.html properties="cas.account-registration.provisioning.syncope" %}
