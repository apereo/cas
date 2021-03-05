---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# REST Audits

Audit events may also be `POST`ed to an endpoint of your choosing. To activate 
this feature, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-rest" %}

The body of the HTTP request is a JSON representation of the audit record. 

{% include casproperties.html properties="cas.audit.rest" %}

