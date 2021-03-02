---
layout: default
title: CAS - Mongo Service Registry
category: Services
---

{% include variables.html %}

# Mongo Service Registry

This registry uses a [MongoDb](https://www.mongodb.org/) instance to load and persist service definitions.
Support is enabled by adding the following module into the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-mongo-service-registry" %}

## Configuration

{% include casproperties.html properties="cas.service-registry.mongo" %}

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
