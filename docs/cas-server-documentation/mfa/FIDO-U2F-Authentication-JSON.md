---
layout: default
title: CAS - U2F - FIDO Universal 2nd Factor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JSON U2F - FIDO Universal Registration

A device repository implementation that collects user device registrations and 
saves them into a JSON file whose path is taught to CAS via settings. This is 
a very modest option and should mostly be used for demo and testing purposes. 
Needless to say, this JSON resource acts as a database that must be available to all CAS server nodes in the cluster.

{% include casproperties.html properties="cas.authn.mfa.u2f.json" %}

Devices stored into the JSON file take on the following format:

```json
{
  "@class" : "java.util.HashMap",
  "devices" : [ "java.util.ArrayList", [ {
    "@class" : "org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration",
    "id" : 1508515100762,
    "username" : "casuser",
    "record" : "{\"keyHandle\":\"keyhandle11\",\"publicKey\":\"publickey1\",\"counter\":1,\"compromised\":false}",
    "createdDate" : "2016-10-15"
  } ] ]
}
```
