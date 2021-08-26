---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# LDAP FIDO2 WebAuthn Multifactor Registration

Device registrations may be kept inside LDAP directories by including the following module in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn-ldap" %}

Device registration records are kept inside a designated configurable multi-valued 
attribute as JSON blobs. The attribute values are parsed
to load, save, update or delete accounts. The content of each attribute
value can be signed/encrypted if necessary. 

{% include casproperties.html properties="cas.authn.mfa.web-authn.ldap" %}
