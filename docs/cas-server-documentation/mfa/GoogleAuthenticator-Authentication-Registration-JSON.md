---
layout: default
title: CAS - JSON Google Authenticator Registration
category: Multifactor Authentication
---

{% include variables.html %}

# JSON Google Authenticator Registration

Registration records may also be kept inside a single JSON file for all users.
The behavior is only activated when a path to a JSON data store file is provided,
and otherwise CAS may fallback to keeping records in memory. This feature is mostly
useful during development and for demo purposes.

<div class="alert alert-warning">:warning: <strong>Single Node</strong><p>Changes to the JSON file are
coordinated within a single CAS server only. Do not point several CAS nodes at the same file, since concurrent
updates from different nodes may overwrite one another.</p></div>

{% include_cached casproperties.html properties="cas.authn.mfa.gauth.json" %}
