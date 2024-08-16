---
layout: default
title: CAS - Configuring Service Contacts
category: Services
---

{% include variables.html %}

# Service Change Management & History

CAS has the ability to track and manage changes to service definitions, 
and to keep a history of changes made to service definitions. Change management and history
tracking sits outside and on top of the core CAS service management facility and as such is designed
to work with any type of service registry supported by CAS. 

Change tracking works by intercepting all operations to the CAS service registry that need to 
modify service definitions, and attempts to store a snapshot of the service definition once after 
the operation is executed. With enough history in place, one can begin to audit changes or review
historical changes. 

This feature is largely backed by the [Javers](https://javers.org/) library.

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-javers" %}

## MongoDb Storage

By default, changes to application definitions and services are audited, tracked and stored
inside a MongoDb database. You'll need to define the connection settings and details in the CAS 
configuration to point to the MongoDb instance and the rest is handled by CAS automatically.

{% include_cached casproperties.html properties="cas.javers.mongo" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="entityHistory" %}
