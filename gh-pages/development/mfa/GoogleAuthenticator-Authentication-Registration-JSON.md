---
layout: default
title: CAS - Google Authenticator Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JSON Google Authenticator Registration

Registration records may also be kept inside a single JSON file for all users.
The behavior is only activated when a path to a JSON data store file is provided,
and otherwise CAS may fallback to keeping records in memory. This feature is mostly
useful during development and for demo purposes.

{% include casproperties.html properties="cas.authn.mfa.gauth.json" %}
