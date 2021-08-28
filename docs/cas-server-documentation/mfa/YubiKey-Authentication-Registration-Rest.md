---
layout: default
title: CAS - YubiKey Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# REST YubiKey Registration

Registration records may be tracked using an external REST API.

{% include_cached casproperties.html properties="cas.authn.mfa.yubikey.rest" %}

The following endpoints are expected to be available and implemented by the REST API:

| METHOD              | Endpoint        | Description
|--------------------------------------------------------------------------------------
| `GET`              | `/`              | Get all registered records.
| `GET`              | `/{user}`        | Get all registered records for the user.
| `DELETE`           | `/`              | Delete all registered records.
| `DELETE`           | `/{user}`        | Delete all registered records for the user.
| `DELETE`           | `/{user}/{id}`   | Delete the registered device by its id from the the registration record for the user.
| `POST`             | `/`              | Store registration records passed as the request body.
