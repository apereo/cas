---
layout: default
title: CAS - Database Authentication
category: Authentication
---
{% include variables.html %}

# Database Authentication

Database authentication is enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jdbc" %}

To learn how to configure database drivers, [please see this guide](../installation/JDBC-Drivers.html).

The parameters that may be passed are as follows:

| Option            | Resource                                                        |
|-------------------|-----------------------------------------------------------------|
| Bind              | See [this guide](Database-Authentication-Bind.html).            |
| Search            | See [this guide](Database-Authentication-Search.html).          |
| Query             | See [this guide](Database-Authentication-Query.html).           |
| Encode            | See [this guide](Database-Authentication-Encode.html).          |
| Stored Procedures | See [this guide](Database-Authentication-StoredProcedure.html). |
