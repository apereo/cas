---
layout: default
title: CAS - Web Flow Customization
category: Webflow Management
---

{% include variables.html %}

# Server-side Sessions - Ticket Registry

If you don't wish to use the native container's strategy for session replication,
and the session storage backend of your choice is not natively supported by Spring Session,
you can use CAS' support for the [Ticket Registry](../ticketing/Configuring-Ticketing-Components.html) to store sessions.

This option translates session data and attributes into a `TST` ticket, and stores it in the configured ticket registry. Note that
by *Session*, we mean the HTTP session that is created by the container such as Apache Tomcat specific to the runtime environment,
and is represented by the `jakarta.servlet.http.HttpSession` object. 

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-session-ticket-registry" %}
