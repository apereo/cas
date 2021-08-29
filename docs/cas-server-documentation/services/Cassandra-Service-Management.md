---
layout: default
title: CAS - Cassandra Service Registry
category: Services
---

{% include variables.html %}

# Cassandra Service Registry

Stores registered service data in [Apache Cassandra](http://cassandra.apache.org/) instances. Services 
are expected to be found/stored in a `casservices` table with a default write 
consistency of `LOCAL_QUORUM` and read consistency of `ONE`.

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-cassandra-service-registry" %}

## Configuration

{% include_cached casproperties.html properties="cas.service-registry.cassandra" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.datastax.driver" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
