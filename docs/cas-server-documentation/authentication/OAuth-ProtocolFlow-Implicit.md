---
layout: default
title: CAS - OAuth Protocol Flow - Token/Implicit
category: Authentication
---
{% include variables.html %}

# OAuth Protocol Flow - Token/Implicit

The `token` type used for the implicit flow is made for UI interactions as well as indirect non-interactive (i.e. JavaScript) applications.

| Endpoint              | Parameters                                               | Response                                                       |
|-----------------------|----------------------------------------------------------|----------------------------------------------------------------|
| `/oauth2.0/authorize` | `response_type=token&client_id=ID&redirect_uri=CALLBACK` | The access token as an anchor parameter of the `CALLBACK` url. |

The implicit flow uses OIDC to implement web sign-in that is very similar to the way SAML and WS-Federation operates. 
The client web app requests and obtains access tokens through the front channel, without the need for secrets or extra backend calls. 

Traditionally, the Implicit Flow is used by applications that were incapable of securely storing secrets. Using this flow 
is no longer considered a best practice for requesting access tokens; new implementations should use 
[Authorization Code Flow with PKCE](OAuth-ProtocolFlow-AuthorizationCode.html). 
