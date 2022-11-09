---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# JWT Secured Authorization Response Mode (JARM) - OpenID Connect Authentication

JWT Secured Authorization Response (JARM) allows for a number of strategies that allow
a new JWT-based mode to encode authorization responses back to the client. This mechanism enhances 
the security of the standard authorization response with support for signing and optional 
encryption of the response. A signed response provides message integrity, sender authentication, 
audience restriction, and protection from mix-up attacks. Encrypting the response provides 
confidentiality of the response parameter values. 

A relying party definition in CAS can be marked to produce JWT authorization responses:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "serviceId": "https://app.example.org/redirect",
  "name": "Sample",
  "id": 1,
  "scopes" : [ "java.util.HashSet", [ "profile", "openid" ] ],
  "supportedResponseTypes": [ "java.util.HashSet", [ "code" ] ],
  "responseMode": "query.jwt"
}
```

JWT authorization responses produced by CAS can be signed and/or encrypted. The signing and encryption
strategy should quite similar to that of ID tokens or access tokens.

The following example shows the JWT claims for a successful `code` authorization response:

```json
{
    "iss": "https://sso.example.com/cas/oidc",
    "aud": "client",
    "exp": 1311281970,
    "code": "OC-1-...",
    "state": "..."
}
```

{% tabs oidcjarmmodes %}
             
{% tab oidcjarmmodes Query %}

The response mode `query.jwt` causes CAS to send the authorization response as HTTP redirect 
to the redirect URI of the client. CAS adds the parameter `response` containing the JWT to 
the query component of the redirect URI:

```bash
HTTP/1.1 302 Found
Location: https://app.example.org/redirect?response=eyJraWQiOiJsYWViIiwiYWxnIjoiRVMyN...
```

{% endtab %}

{% tab oidcjarmmodes Fragment %}

The response mode `fragment.jwt` causes CAS to send the authorization response as HTTP redirect to 
the redirect URI of the client. The authorization server adds the parameter `response` containing 
the JWT to the fragment component of the redirect URI:

```bash
HTTP/1.1 302 Found
Location: https://app.example.org/redirect#response=eyJraWQiOiJsYWViIiwiYWxnIjoiRVMyN...
```

{% endtab %}

{% tab oidcjarmmodes Query %}

The response mode `query.jwt` causes CAS to send the authorization response as HTTP redirect
to the redirect URI of the client. CAS adds the parameter `response` containing the JWT to
the query component of the redirect URI:

```bash
HTTP/1.1 302 Found
Location: https://app.example.org/redirect?response=eyJraWQiOiJsYWViIiwiYWxnIjoiRVMyN...
```

{% endtab %}

{% tab oidcjarmmodes Form Post %}

The response mode `form_post.jwt` causes CAS to `POST` the authorization response to
the redirect URI of the client. The `response` parameter containing the JWT is encoded as HTML 
form value that is auto-submitted and thus is transmitted via the HTTP `POST` method to the client, 
with the result parameters being encoded in the body using the `application/x-www-form-urlencoded` format.

{% endtab %}

{% endtabs %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.oidc.jarm" %}
