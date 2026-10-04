---
layout: default
title: CAS - Google Authenticator Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# DynamoDb Google Authenticator Registration

Registration records and tokens may be kept inside a DynamoDb instance via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gauth-dynamodb" %}

Registration records are keyed by their identifier, and looked up by user through a global secondary index
named `useridIndex` on the `userid` attribute (projecting all attributes). CAS creates the index along with the table,
and adds it to an existing table at startup.

<div class="alert alert-warning">:warning: <strong>Usage Warning</strong><p>When table creation on startup is
turned off, the index must be created ahead of time. Lookups by user fail while the index is missing or still being
built on an existing table.</p></div>

{% include_cached casproperties.html properties="cas.authn.mfa.gauth.dynamo-db" %}
