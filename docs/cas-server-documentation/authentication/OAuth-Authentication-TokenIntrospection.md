---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# Token Introspection - OAuth Authentication

The contents of access or refresh tokens issued by CAS are typically opaque to clients (though access tokens may also be conditionally 
[issued as JWTs](OAuth-Authentication-JWT-AccessTokens.html)). This means that the client does not need to know anything about the
content or structure of the token itself, if there is any. However, there is still a large amount of metadata that may be attached to a
token, such as its current validity, approved scopes, and information about the context in which the token was issued. This specification defines 
a [protocol](https://tools.ietf.org/html/rfc7662) that allows authorized protected resources to query the authorization server to determine 
the set of metadata for a given token that was presented to them by an OAuth 2.0 client. This metadata includes whether or not the token 
is currently active (or if it has expired or otherwise been revoked), what rights of access the token carries, and the authorization context 
in which the token was granted (including who authorized the token and which client it was issued to). Token introspection allows a protected resource to
query this information regardless of whether or not it is carried in the token itself, allowing this method to be used along with or
independently of structured token values.

The introspection endpoint expects HTTP basic authentication with OAuth2 service `client_id` and `client_secret`. The following is 
an example response to an introspection request for a token:

```json
{
    "active": true,
    "client_id": "l2345678",
    "username": "jdoe",
    "scope": "read write manage",
    "sub": "Z5O3upPC88QrAjx00dis",
    "aud": "https://protected.example.net/resource",
    "iss": "https://cas.example.com/oidc",
    "exp": 1419356238,
    "iat": 1419350238
}
```   
   
## JWT Responses for Token Introspection

The introspection response is a plain JSON object. However, there are use cases where the resource server 
requires stronger assurance that the CAS server issued the token introspection response for an 
access token, including cases where the CAS server assumes liability for the content of the token introspection response. In such use cases it 
may be useful or even required to return a signed JWT as the introspection response. 

A token introspection request may ask for a JWT introspection response by sending an introspection request 
with an `Accept` HTTP header field set to `application/token-introspection+jwt`. The introspection endpoint responds with a JWT, 
setting the `Content-Type` HTTP header field to `application/token-introspection+jwt` and the JWT `typ` ("type") header 
parameter to `token-introspection+jwt`.

The example response JWT payload contains the following JSON document:
 
```json
{
    "iss":"https://cas.example.com/oidc",
    "aud":"https://rs.example.com/resource",
    "iat":1514797892,
    "token_introspection":
    {
        "active":true,
        "iss":"https://as.example.com/",
        "aud":"https://rs.example.com/resource",
        "iat":1514797822,
        "exp":1514797942,
        "client_id":"paiB2goo0a",
        "scope":"read write dolphin",
        "sub":"Z5O3upPC88QrAjx00dis",
        "birthdate":"1982-02-01",
        "given_name":"John",
        "family_name":"Doe",
        "jti":"t1FoCCaZd4Xv4ORJUWVUeTZfsKhW30CQCrWDDjwXy6w"
    }
}
```
      
Signing and encryption algorithms used to process JWT responses for token introspection can be defined on a [per-client basis](OAuth-Authentication-Clients.html). 
