---
layout: default
title: CAS - Mongo Service Registry
category: Services
---

{% include variables.html %}

# Mongo Service Registry

This registry uses a [MongoDb](https://www.mongodb.org/) instance to load and persist service definitions.
Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mongo-service-registry" %}

## Configuration

{% include_cached casproperties.html properties="cas.service-registry.mongo" %}

## Change Streams

When MongoDb runs as a [replica set](https://www.mongodb.com/docs/manual/replication/), CAS watches the service
registry collection through a [change stream](https://www.mongodb.com/docs/manual/changeStreams/) and reloads service
definitions shortly after they change, on every CAS node. Changes made by another CAS node, or directly in MongoDb,
are picked up in seconds instead of at the next [scheduled reload](Service-Management-Caching-Reloading.html).

Change streams are used only when both of the following hold:

- A replica set is defined: the `replica-set` setting is set, or the `client-uri` names one with its `replicaSet` option
  or uses the `mongodb+srv` scheme.
- The server confirms that it is a member of a replica set. Otherwise, such as for a standalone server or a sharded
  cluster reached through `mongos`, CAS logs this once and relies on the scheduled reload alone.

Changes that arrive close together, such as a bulk import, result in a single reload once no change has been seen for
the configured quiet period; under a steady stream of changes, CAS still reloads at least once every ten quiet periods.
A configuration refresh restarts the change stream with the refreshed settings. If the stream is interrupted, CAS resumes it where it left off; when that is no longer
possible, it reloads all service definitions and starts a new stream. The database user needs the `changeStream`
and `find` privileges on the collection, both of which the built-in `read` role grants. Each CAS node keeps one
long-running request open against the replica set, which holds one connection from the pool while it waits.

<div class="alert alert-info">:information_source: <strong>Scheduled Reloads</strong><p>
Keep the scheduled reload enabled as a safety net, since it also covers changes made while a node could not reach MongoDb.
With change streams in place, its interval can be much longer than the default. Disabling the scheduler instead makes
CAS consult the registry for every request that matches no cached definition.
</p></div>

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
