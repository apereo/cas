---
layout: default
title: CAS - U2F - FIDO Universal 2nd Factor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# CouchDb U2F - FIDO Universal Registration

Device registrations may be kept inside a CouchDb instance by including the following module in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-u2f-couchdb" %}

{% include casproperties.html properties="cas.authn.mfa.u2f.couch-db" %}
