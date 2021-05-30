---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# REST FIDO2 WebAuthn Multifactor Registration

Device registrations may be managed using an external REST API by including the following module in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn-rest" %}

The following parameters are passed:

| Operation        | Parameters      | Description      | Result
|------------------|-----------------|------------------|-----------------------------------------------------
| `GET`            | N/A             | Retrieve all records.     | `200` status code; Collection of JSON records in the body.
| `GET`            | `username`      | Retrieve all records for user.  | `200` status code Collection of JSON records in the body.
| `POST`           | Collection of records as JSON body | Store/Update registered devices. | `200`.

{% include casproperties.html properties="cas.authn.mfa.web-authn.rest" %}
