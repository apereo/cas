---
layout: default
title: CAS - Deny Authentication
category: Authentication
---
{% include variables.html %}


# Deny Authentication

Denying authentication allows CAS to reject access to a set of credentials.
Those that fail to match against the predefined set will blindly be accepted.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-generic" %}

{% include casproperties.html properties="cas.authn.reject" %}
