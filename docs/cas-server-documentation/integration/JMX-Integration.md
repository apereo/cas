---
layout: default
title: CAS - JMX Integration
category: Integration
---

{% include variables.html %}

# JMX Integration

The JMX support in CAS provides you with the features to easily and transparently integrate your CAS deployment into a JMX infrastructure. These features are 
designed to work without coupling CAS components to either Spring or JMX interfaces and classes. CAS components need not be aware of either JMX in order 
to take advantage of the Spring JMX features. Invoking JMX operations provided by CAS managed resources can be done via the likes of Java's `jconsole` tool.

The following *wrapper* components are registered into a JMX infrastructure and sit on top of a well-defined CAS feature, 
concept or component to provide remote operations or insight:

- Registered services: inventory, lookup, counts and reload.
- Ticket registry: counts, filtered ticket and session listings, and expired-ticket cleanup.
- Authentication: registered handlers, multifactor providers and service-specific provider availability.
- Principal attributes: cache availability and invalidation.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jmx" %}
