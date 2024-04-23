---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Regex Attribute Authentication Interrupt

This strategy allows one to define regular expression patterns in CAS settings that would be 
matched against attributes names and values. If a successful match is produced while CAS 
examines the collection of both authentication and principal attributes, the 
authentication flow would be interrupted.

{% include_cached casproperties.html properties="cas.interrupt.regex" %}
