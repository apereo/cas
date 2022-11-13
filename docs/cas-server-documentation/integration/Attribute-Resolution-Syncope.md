---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Apache Syncope Attribute Resolution

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-syncope-authentication" %}

The following configuration describes how to fetch and retrieve attributes from [Apache Syncope](https://syncope.apache.org/).

{% include_cached casproperties.html properties="cas.authn.attribute-repository.syncope" %}

