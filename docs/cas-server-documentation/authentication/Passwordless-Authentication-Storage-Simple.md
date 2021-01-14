---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Simple Passwordless Authentication Storage

This strategy provides a static map of usernames that are linked to their 
method of contact, such as email or phone number. It is best used
for testing and demo purposes. The key in the map is taken to be the username 
eligible for authentication while the value can either be an email
address or phone number that would be used to contact the user with issued tokens.

{% include casproperties.html properties="cas.authn.passwordless.accounts.simple" %}
