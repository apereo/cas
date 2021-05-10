---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# REST Passwordless Authentication Tokens

This strategy allows one design REST endpoints in charge of managing 
tokens and their expiration policy entirely. 
CAS continues to generate tokens and the endpoint is only acting as a 
facade to the real token store, receiving tokens from CAS
in an encrypted fashion. 

{% include casproperties.html properties="cas.authn.passwordless.tokens.rest" %}

The following operations need to be supported by the endpoint:

| HTTP Method | Description                               | Parameter(s)          | Response
|-------------|-------------------------------------------|-----------------------|--------------------------------
| `GET`       | Locate tokens for the user.               | `username`            | Token in the response body.
| `DELETE`    | Delete all tokens for the user.           | `username`            | N/A
| `DELETE`    | Delete a single token for the user.       | `username`, `token`   | N/A
| `POST`      | Save a token for the user.                | `username`, `token`   | N/A
