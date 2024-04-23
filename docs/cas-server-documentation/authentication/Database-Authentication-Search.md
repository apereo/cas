---
layout: default
title: CAS - Database Authentication
category: Authentication
---
{% include variables.html %}

# Search Database Authentication

Searches for a user record by querying against a username and password;
the user is authenticated if at least one result is found.

{% include_cached casproperties.html properties="cas.authn.jdbc.search" %}
