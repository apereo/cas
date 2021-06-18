---
layout: default
title: CAS - JPA Service Registry
category: Services
---

{% include variables.html %}

# JPA Service Registry

Stores registered service data in a database.

Support is enabled by adding the following module into the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-service-registry" %}

To learn how to configure database drivers, [please see this guide](../installation/JDBC-Drivers.html).

## Configuration

{% include casproperties.html properties="cas.service-registry.jpa" %}

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize 
itself from default JSON service definitions available to 
CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
