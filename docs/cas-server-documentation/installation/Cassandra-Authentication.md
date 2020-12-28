---
layout: default
title: CAS - Apache Cassandra Authentication
category: Authentication
---
{% include variables.html %}


# Apache Cassandra Authentication

Verify and authenticate credentials using [Apache Cassandra](http://cassandra.apache.org/).

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-cassandra-authentication" %}

{% include {{ version }}/cassandra-authentication-configuration.md %}

{% include {{ version }}/cassandra-configuration.md configKey="cas.authn.cassandra" %}

{% include {{ version }}/principal-transformation.md configKey="cas.authn.cassandra" %}

{% include {{ version }}/password-encoding.md configKey="cas.authn.cassandra" %}

