---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JSON FIDO2 WebAuthn Multifactor Registration

A device repository implementation that collects user device registrations and saves them into a JSON file whose 
path is taught to CAS via settings. This is a very modest option and should mostly be used for demo and testing 
purposes. Needless to say, this JSON resource acts as a database that must be available to all CAS server nodes in the cluster.

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.json" %}
