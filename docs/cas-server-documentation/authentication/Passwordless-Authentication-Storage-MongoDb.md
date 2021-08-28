---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# MongoDb Passwordless Authentication Storage

This strategy allows one to locate a user record in MongoDb. The designated MongoDb 
collection is expected to carry objects of type `PasswordlessUserAccount` in JSON format. 

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-passwordless-mongo" %}

{% include_cached casproperties.html properties="cas.authn.passwordless.accounts.mongo" %}
