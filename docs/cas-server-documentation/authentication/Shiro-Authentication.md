---
layout: default
title: CAS - Shiro Authentication
category: Authentication
---
{% include variables.html %}

# Shiro Authentication

CAS support handling the authentication event via [Apache Shiro](http://shiro.apache.org/).

## Components

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-shiro-authentication" %}

## Shiro Configuration

Apache Shiro supports retrieving and checking roles and permissions for an authenticated
subject. CAS exposes a modest configuration to enforce roles and permissions as part
of the authentication, so that in their absence, the authentication may fail.
While by default these settings are optional, you may configure roles and/or permissions
for the given authentication handler to check their presence and report back.

{% include casproperties.html properties="cas.authn.shiro" %}

Sample `shiro.ini` that needs be placed on the classpath based on the example above:

```ini
[main]
cacheManager = org.apache.shiro.cache.MemoryConstrainedCacheManager
securityManager.cacheManager = $cacheManager

[users]
casuser = Mellon, admin

[roles]
admin = system,admin,staff,superuser:*
```

## Logging

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.apache.shiro" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
