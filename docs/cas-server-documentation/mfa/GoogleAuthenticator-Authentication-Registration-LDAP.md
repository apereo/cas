---
layout: default
title: CAS - Google Authenticator Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# LDAP Google Authenticator Registration

Registration records may be kept inside LDAP/AD systems via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gauth-ldap" %}

Account registration records are kept inside a designated configurable multi-valued 
attribute as JSON blobs. The attribute values are parsed
to load, save, update or delete accounts. The content of each attribute value can be signed/encrypted if necessary. 

{% include_cached casproperties.html properties="cas.authn.mfa.gauth.ldap" %}
