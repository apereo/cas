---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# REST Device Storage - Multifactor Authentication Trusted Device/Browser

If you wish to completely delegate the management, verification and 
persistence of user decisions, you may design a REST API
which CAS shall contact to verify user decisions and remember those for later.

Support is provided via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-trusted-mfa-rest" %}

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.rest" %}

## Retrieve Trusted Records

A `GET` request that returns all trusted authentication records that are valid and not-expired.

```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET "${endpointUrl}/[principal|date]"
```

Response payload may produce a collection of objects that contain:

```json
[
    {
      "principal": "casuser",
      "deviceFingerprint": "...",
      "recordDate": "YYYY-MM-dd",  
      "expirationDate":  "YYYY-MM-dd", 
      "name": "Office",
      "recordKey": "..."
    }
]
```

## Store Trusted Records

A `POST` request that stores a newly trusted device record.

```bash
curl -H "Content-Type: application/json" -X POST -d '${json}' ${endpointUrl}
```

`POST` data will match the following block:

```json
{
    "principal": "...",
    "deviceFingerprint": "...",
    "recordDate": "...",    
    "expirationDate":  "YYYY-MM-dd", 
    "name": "...",
    "recordKey": "..."
}
```

Response payload shall produce a `200` http status code to indicate a successful operation.
