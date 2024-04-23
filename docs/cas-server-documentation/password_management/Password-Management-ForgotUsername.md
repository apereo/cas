---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Forgot Username - Password Management

CAS provides the ability to retrieve a forgotten username. This behavior is tightly integrated with the password
management functionality where each account storage service is allowed to retrieve the user record using a supplied
identifier from the user (typically an email address) and supply the username via email notifications, etc.

{% include_cached casproperties.html properties="cas.authn.pm.forgot-username" %}
