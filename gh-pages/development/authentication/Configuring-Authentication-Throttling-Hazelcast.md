---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# Hazelcast Throttling Authentication Attempts

This feature uses a distributed Hazelcast map to record throttled authentication attempts. 
This component requires and depends on the [CAS auditing functionality](../audits/Audits.html)

Enable the following module in your configuration overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-hazelcast" %}

{% include {{ version }}/hazelcast-configuration.md configKey="cas.authn.throttle.hazelcast" %}
