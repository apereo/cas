---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
category: Acceptable Usage Policy
---

{% include variables.html %}

# CouchDb Acceptable Usage Policy

CAS can be configured to use a CouchDb instance as the storage mechanism. Upon accepting the
policy, the adopter is expected to provide a collection name where the decision is kept and
the document is assumed to contain a `username` column as well as one that matches the AUP attribute name defined.

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-aup-couchdb" %}

{% include casproperties.html properties="cas.acceptable-usage-policy.couch-db" %}
