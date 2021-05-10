---
layout: default
title: CAS - LDAP Service Registry
category: Services
---

{% include variables.html %}

# LDAP Service Registry

Service registry implementation which stores the services in a LDAP Directory 
and attempts to *map* service records to LDAP entries in order to configure 
settings for retrieval, search and persistence of service definitions. By default, 
entries are assigned the `objectclass` that is `casRegisteredService` attribute and are looked up by the `uid` attribute.

Support is enabled by adding the following module into the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-ldap-service-registry" %}

## Configuration

The default mapper has support for the following optional items:

| Field                             | Default Value
|-----------------------------------|---------------------------------------------------
| `objectClass`                     | casRegisteredService
| `serviceDefinitionAttribute`      | description
| `idAttribute`                     | uid

Service definitions are by default stored inside the `serviceDefinitionAttribute` attribute as
JSON objects. The format and syntax of the JSON is identical to that of
[JSON Service Registry](JSON-Service-Management.html). That's all, as far as the schema goes.

{% include casproperties.html properties="cas.service-registry.ldap" %}

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
