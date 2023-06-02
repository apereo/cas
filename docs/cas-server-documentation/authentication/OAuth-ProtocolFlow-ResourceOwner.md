---
layout: default
title: CAS - OAuth Protocol Flow - Resource Owner Credentials
category: Authentication
---
{% include variables.html %}

# OAuth Protocol Flow - Resource Owner Credentials

The `password` grant type allows the OAuth client to directly send the user's credentials to the OAuth server.
This grant is a great user experience for trusted first party clients both on the web and in native device applications.

| Endpoint                | Parameters                                                                                                  | Response          |
|-------------------------|-------------------------------------------------------------------------------------------------------------|-------------------|
| `/oauth2.0/accessToken` | `grant_type=password&client_id=ID`<br/>`&client_secret=<SECRET>`<br/>`&username=USERNAME&password=PASSWORD` | The access token. |

Because there is no `redirect_uri` specified by this grant type, the service identifier recognized by CAS and matched in the service registry is taken as the `client_id` instead. You may optionally also pass along a `service` or `X-service` header value that identifies the target application url. The header value must match the OAuth service definition in the registry that is linked to the client id.
