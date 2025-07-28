---
layout: default
title: CAS - Web Flow Customization
category: Webflow Management
---

{% include variables.html %}

# Server-side Sessions - Ticket Registry

If you don't wish to use the native container's strategy for session replication,
and the session storage backend of your choice is not natively supported by Spring Session,
you can use CAS's support for the Ticket Registry to store sessions.

This option translates the session into a `TST` ticket, and stores it in the configured ticket registry.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-session-ticket-registry" %}
