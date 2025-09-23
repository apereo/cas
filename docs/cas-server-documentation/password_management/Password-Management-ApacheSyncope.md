---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Password Management - Apache Syncope
         
CAS support handling password management operations via [Apache Syncope](https://syncope.apache.org/). This
is done by using Syncope REST APIs. Support includes the ability to change passwords 
voluntarily, or forcefully as part of the authentication process. 

{% include_cached casproperties.html properties="cas.authn.pm.syncope" %}
