---
layout: default
title: CAS - CosmosDb Service Registry
category: Services
---

{% include variables.html %}

# CosmosDb Service Registry

Stores registered service data in an [Azure CosmosDb](https://docs.microsoft.com/en-us/azure/cosmos-db/introduction) instance.

Support is enabled by adding the following module into the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-cosmosdb-service-registry" %}

## Configuration

{% include casproperties.html properties="cas.service-registry.cosmos-db" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.microsoft.azure" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```


## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
