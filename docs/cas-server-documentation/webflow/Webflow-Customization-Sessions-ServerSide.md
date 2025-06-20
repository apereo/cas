---
layout: default
title: CAS - Web Flow Customization
category: Webflow Management
---

{% include variables.html %}

# Server-side Sessions

In the event that you wish to use server-side session storage for managing the
webflow session, you will need to enable this behavior
via CAS properties. 

{% include_cached casproperties.html properties="cas.webflow.session" 
thirdPartyStartsWith="spring.session" 
excludes=".jdbc,.hazelcast,.mongodb,.redis" %}

Doing so will likely require you to also enable sticky sessions and/or session replication in a clustered deployment of CAS.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
Generally speaking, you do not need to enable server-side sessions unless you have a 
rather specialized deployment or are in need of features that store bits and pieces 
of data into a sever-backed session object. It is recommended that you stick with 
the default client-side session storage and only switch if and when mandated by a specific CAS behavior.</p></div>

## Hazelcast Session Replication

If you don't wish to use the native container's strategy for session replication,
you can use CAS's support for Hazelcast session replication.

This feature is enabled via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-session-hazelcast" %}

{% include_cached casproperties.html 
properties="cas.webflow.session.server.hazelcast" 
thirdPartyStartsWith="spring.session.hazelcast,spring.session.hazelcast" %}

## Redis Session Replication

If you don't wish to use the native container's strategy for session replication,
you can use CAS's support for Redis session replication.

This feature is enabled via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-session-redis" %}

{% include_cached casproperties.html 
thirdPartyStartsWith="spring.session.redis,spring.redis" %}

## MongoDb Session Replication

If you don't wish to use the native container's strategy for session replication,
you can use CAS's support for Mongo session replication.

This feature is enabled via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-session-mongo" %}

{% include_cached casproperties.html 
thirdPartyStartsWith="spring.session.mongodb,spring.data.mongodb" %}

## JDBC Session Replication

If you don't wish to use the native container's strategy for session replication,
you can use CAS's support for JDBC session replication.

This feature is enabled via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-session-jdbc" %}

{% include_cached casproperties.html 
thirdPartyStartsWith="spring.session.jdbc,spring.datasource" %}

