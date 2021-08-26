---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Grouper - Multifactor Authentication Triggers

MFA can be triggered by [Grouper](https://incommon.org/software/grouper/)
groups to which the authenticated principal is assigned.
Groups are collected by CAS and then cross-checked against all available/configured MFA providers.
The group's comparing factor **MUST** be defined in CAS to activate this behavior
and it can be based on the group's name, display name, etc where
a successful match against a provider id shall activate the chosen MFA provider.

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-grouper" %}

You will also need to ensure `grouper.client.properties` is available on the classpath
with the following configured properties:

```properties
grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
grouperClient.webService.login = banderson
grouperClient.webService.password = password
```

{% include casproperties.html properties="cas.authn.mfa.triggers.grouper" %}
