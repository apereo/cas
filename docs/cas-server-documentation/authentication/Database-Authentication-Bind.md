---
layout: default
title: CAS - Database Authentication
category: Authentication
---
{% include variables.html %}

# Bind Database Authentication

Authenticates a user by attempting to create a database connection using the username and (hashed) password.

{% include_cached casproperties.html properties="cas.authn.jdbc.bind" %}
