---
layout: default
title: CAS - REST FIDO2 WebAuthn Multifactor Registration
category: Multifactor Authentication
---

{% include variables.html %}

# REST FIDO2 WebAuthn Multifactor Registration

Keep FIDO2 WebAuthn device registrations behind a REST API you provide, which CAS calls to read, save and remove them.

Device registrations may be managed using an external REST API by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn-rest" %}

The following parameters are passed:

| Operation | Parameters                         | Description                      | Result                                                     |
|-----------|------------------------------------|----------------------------------|------------------------------------------------------------|
| `GET`     | N/A                                | Retrieve all records.            | `200` status code; Collection of JSON records in the body. |
| `GET`     | `username`                         | Retrieve all records for user.   | `200` status code Collection of JSON records in the body.  |
| `POST`    | Collection of records as JSON body | Store/Update registered devices. | `200`.                                                     |

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.rest" %}
