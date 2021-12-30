---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud MongoDb

Spring Cloud Configuration Server is able to locate properties entirely from a MongoDb instance.

Support is provided via the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-mongo" %}

<div class="alert alert-info mt-3"><strong>Usage</strong><p>The configuration modules provide here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

Note that to access and review the collection of CAS properties,
you will need to use your own native tooling for MongoDB to configure and inject settings.

MongoDb documents are required to be found in the collection `MongoDbProperty`, as the following document:

```json
{
    "id": "kfhf945jegnsd45sdg93452",
    "name": "the-setting-name",
    "value": "the-setting-value"
}
```

{% include_cached casproperties.html properties="cas.spring.cloud.mongo" %}
