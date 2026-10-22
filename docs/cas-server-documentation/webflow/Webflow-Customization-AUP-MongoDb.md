---
layout: default
title: CAS - MongoDb Acceptable Usage Policy
category: Acceptable Usage Policy
---

{% include variables.html %}

# MongoDb Acceptable Usage Policy

CAS can be configured to use a MongoDb instance as the storage mechanism. Upon accepting the policy, the decision is kept in the configured collection, in a document with a `username` field and a field that matches the AUP attribute name defined; the document is created if the user does not have one yet. The decision is read back at later logins when the principal does not carry the AUP attribute itself.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-aup-mongo" %}

{% include_cached casproperties.html properties="cas.acceptable-usage-policy.mongo" %}

# Multitenancy

Configuration settings for MongoDb can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Tenant-Properties.html) for more information.
