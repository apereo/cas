---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication JWKS Storage - REST

Keystore generation can be outsourced to an external REST API. Endpoints must be designed to
accept/process `application/json` and generally should return a `2xx` response status code. 

The following requests are made by CAS to the endpoint:

| Operation | Parameters            | Description                             | Result                                             |
|-----------|-----------------------|-----------------------------------------|----------------------------------------------------|
| `GET`     | N/A                   | Retrieve the keystore, or generate one. | `2xx` status code; JWKS resource in response body. |
| `POST`    | JWKS in request body. | Store the keystore.                     | `2xx` status code.                                 |

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.rest" %}
