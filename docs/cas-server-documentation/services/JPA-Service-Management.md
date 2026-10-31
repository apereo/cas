---
layout: default
title: CAS - JPA Service Registry
category: Services
---

{% include variables.html %}

# JPA Service Registry

Stores registered service data in a database.

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-service-registry" %}

To learn how to configure database drivers, [please see this guide](../installation/JDBC-Drivers.html).

## Configuration

{% include_cached casproperties.html properties="cas.service-registry.jpa" %}

## Service Identifiers

A service saved without an identifier is given a random positive one, as with other service registries. A service that
carries an identifier, such as one imported from another registry, is stored with it. Rows created by earlier versions
keep the identifiers the database sequence gave them.

## Loading Services

Each time CAS reloads its services, the registry reads the identifier and the JSON body of every stored definition.
A definition whose body has not changed since the previous load is not deserialized again; CAS keeps the service it
built from that body last time. Changes written to the table directly, including by another CAS node, change the body
and are picked up on the next reload. Changes made to a service object in memory and never saved are not undone by a
reload while the stored definition stays the same.

Services saved in bulk are flushed to the database every `batch-size`{: .cas-setting} definitions.

The table declares indexes on the service identifier and the service name, which lookups by exact service id or name
use. Schema updates add them to an existing table; with schema generation turned off, create them by hand:

```sql
CREATE INDEX idx_regsvc_service_id ON registered_services (service_id);
CREATE INDEX idx_regsvc_name ON registered_services (name);
```

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize 
itself from default JSON service definitions available to 
CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
