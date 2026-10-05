---
layout: default
title: CAS - MongoDb Google Authenticator Registration
category: Multifactor Authentication
---

{% include variables.html %}

# MongoDb Google Authenticator Registration

Registration records and tokens may be kept inside a MongoDb instance, via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gauth-mongo" %}

Usernames are matched ignoring case but not accents. CAS creates an index named `username_collated` on the `username`
field with the same collation at startup, so lookups by user do not scan the collection. If CAS cannot create it, for
example for lack of privileges, it logs a warning and lookups still work, without the index.

{% include_cached casproperties.html properties="cas.authn.mfa.gauth.mongo" %}
