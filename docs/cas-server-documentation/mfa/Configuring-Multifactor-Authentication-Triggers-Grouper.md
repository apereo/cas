---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Grouper - Multifactor Authentication Triggers

MFA can be triggered by [Grouper](https://github.com/Internet2/grouper) groups to which the authenticated principal is assigned.
Groups are collected by CAS and then cross-checked against all available/configured MFA providers.
The group's comparing factor **MUST** be defined in CAS to activate this behavior
and it can be based on the group's name, display name, etc where
a successful match against a provider id shall activate the chosen MFA provider.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-grouper" %}

You will also need to ensure `grouper.client.properties` is available on the classpath
with the following configured properties:

```properties
grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
grouperClient.webService.login = banderson
grouperClient.webService.password = password
```

{% include_cached casproperties.html properties="cas.authn.mfa.triggers.grouper" %}

You may also consider externalizing the Grouper client configuration file
by modifying `/src/main/resources/grouper.client.properties` to contain the following:

```properties
grouperClient.config.hierarchy = classpath:/grouper.client.base.properties,file:/etc/cas/config/grouper.client.properties
```

...where `/etc/cas/config/grouper.client.properties` would then contain the actual Grouper related settings.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="edu.internet2.middleware" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
