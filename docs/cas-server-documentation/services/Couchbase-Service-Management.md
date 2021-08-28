---
layout: default
title: CAS - Couchbase Service Registry
category: Services
---

{% include variables.html %}

# Couchbase Service Registry

Couchbase integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-couchbase-service-registry" %}

[Couchbase](http://www.couchbase.com) is a highly available, open source NoSQL database server based on
[Erlang/OTP](http://www.erlang.org) and its mnesia database. The intention of this registry is to leverage the capability of Couchbase server to provide high availability to CAS.

## Configuration

{% include_cached casproperties.html properties="cas.service-registry.couchbase" %}

The Couchbase integration currently assumes that the service registries are stored
in their own buckets. Optionally set passwords for the buckets, optionally setup
redundancy and replication as per normal Couchbase configuration.

The only truly mandatory setting is the list of nodes.
The other settings are optional, but this is designed to store data in buckets
so in reality the bucket property must also be set.

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.


## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following
levels:

```xml
...
<Logger name="com.couchbase" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
