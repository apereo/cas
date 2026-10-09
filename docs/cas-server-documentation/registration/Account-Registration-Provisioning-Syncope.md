---
layout: default
title: CAS - Account (Self-Service) Registration - Apache Syncope Provisioning
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration - Apache Syncope Provisioning

Create the accounts users register through CAS in Apache Syncope.

Account registration requests can be submitted to Apache Syncope. Support is enabled by including the 
following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-syncope-authentication" %}

{% include_cached casproperties.html properties="cas.account-registration.provisioning.syncope" %}
