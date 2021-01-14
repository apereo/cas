---
layout: default
title: CAS - U2F - FIDO Universal 2nd Factor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# REST U2F - FIDO Universal Registration

Device registrations may be managed via REST APIs. Endpoints must be designed to 
accept/process `application/json`. The syntax for he collection of devices passed back and 
forth is designed in JSON and is identical to the JSON structure defined above.

The following parameters are passed:

| Operation        | Parameters      | Description      | Result
|------------------|-----------------|------------------|----------------------------------------------------
| `GET` | N/A    | Retrieve all registered devices.     | `200` status code; Collection of registered devices as JSON in response body.
| `POST` | Collection of registered devices as JSON | Store registered devices. | `200`.
| `DELETE` | N/A | Delete all device records | `200`.
| `DELETE` | `/${id}`  as a path variable | Delete all device records matching that identifier. | `200`.

{% include casproperties.html properties="cas.authn.mfa.u2f.rest" %}
