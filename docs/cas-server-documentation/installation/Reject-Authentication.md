---
layout: default
title: CAS - Deny Authentication
category: Authentication
---

# Deny Authentication

Denying authentication allows CAS to reject access to a set of credentials.
Those that fail to match against the predefined set will blindly be accepted.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-generic" %}

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#reject-users-authentication).
