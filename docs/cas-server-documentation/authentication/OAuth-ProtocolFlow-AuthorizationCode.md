---
layout: default
title: CAS - OAuth Protocol Flow - Authorization Code
category: Authentication
---
{% include variables.html %}

# OAuth Protocol Flow - Authorization Code

The authorization code type is made for UI interactions: the user will enter credentials, shall receive a code and 
will exchange that code for an access token.

| Endpoint                | Parameters                                                                                               | Response                                         |
|-------------------------|----------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| `/oauth2.0/authorize`   | `response_type=code&client_id=<ID>&redirect_uri=<CALLBACK>`                                              | OAuth code as a parameter of the `CALLBACK` url. |
| `/oauth2.0/accessToken` | `grant_type=authorization_code&client_id=ID`<br/>`&client_secret=SECRET&code=CODE&redirect_uri=CALLBACK` | The access token.                                |

## Proof Key Code Exchange (PKCE)

The [Proof Key for Code Exchange](https://tools.ietf.org/html/rfc7636) (PKCE, pronounced pixie) extension describes a 
technique for public clients to mitigate the threat of having the authorization code intercepted. The technique involves 
the client first creating a secret, and then using that secret again when exchanging the authorization code for an access 
token. This way if the code is intercepted, it will not be useful since the token request relies on the initial secret.

The authorization code type at the authorization endpoint `/oauth2.0/authorize` is able to accept the following parameters to activate PKCE:

| Parameter               | Description                                                                       |
|-------------------------|-----------------------------------------------------------------------------------|
| `code_challenge`        | The code challenge generated using the method below.                              |
| `code_challenge_method` | `plain`, `S256`. This parameter is optional, where `plain` is assumed by default. |

The `/oauth2.0/accessToken`  endpoint is able to accept the following parameters to activate PKCE:

| Parameter       | Description                                                                                                          |
|-----------------|----------------------------------------------------------------------------------------------------------------------|
| `code_verifier` | The original code verifier for the PKCE request, that the app originally generated before the authorization request. |

If the method is `plain`, then the CAS needs only to check that the provided `code_verifier` matches the expected `code_challenge` string.
If the method is `S256`, then the CAS should take the provided `code_verifier` and transform it using the same method the client will have used initially. This means calculating the SHA256 hash of the verifier and base64-url-encoding it, then comparing it to the stored `code_challenge`.

If the verifier matches the expected value, then the CAS can continue on as normal, issuing an access token and responding appropriately.

