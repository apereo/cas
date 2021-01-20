---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# CouchDb Device Storage - Multifactor Authentication Trusted Device/Browser

User decisions may also be kept inside a CouchDb instance.

Support is provided via the following module:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-trusted-mfa-couchdb" %}

{% include casproperties.html properties="cas.authn.mfa.trusted.couch-db" %}
