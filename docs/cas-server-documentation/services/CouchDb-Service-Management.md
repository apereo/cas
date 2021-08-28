---
layout: default
title: CAS - CouchDB Service Registry
category: Services
---

{% include variables.html %}

# CouchDB Service Registry

CouchDB integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-couchdb-service-registry" %}

[CouchDB](http://couchdb.apache.org/) is a highly available, open source NoSQL database server based on
[Erlang/OTP](http://www.erlang.org) and its mnesia database. The intention of this registry is to leverage the capability of CouchDB
server to provide high availability to CAS across multiple data centers.

## Configuration

{% include_cached casproperties.html properties="cas.service-registry.couch-db" %}

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following
levels:

```xml
...
<Logger name="org.apache.couchdb" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
