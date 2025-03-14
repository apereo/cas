---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Amazon Kinesis Firehose Audits

AWS Security Lake is a managed service that centralizes security-related logs 
and analytics from various AWS and third-party sources. You can send CAS audit events 
to Security Lake using AWS Firehose.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-audit-aws-firehose" %}
         
{% include_cached casproperties.html properties="cas.audit.amazon-firehose" %}
