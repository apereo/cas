---
layout: default
title: CAS - OAuth Protocol Flow - Client Credentials
category: Authentication
---
{% include variables.html %}

# OAuth Protocol Flow - Client Credentials

This grant is suitable for machine-to-machine authentication
where a specific user’s permission to access data is not required. It is used by clients to obtain an access token outside of 
the context of a user to access resources about themselves rather than to access a user's resources.

| Endpoint                | Parameters                                                            | Response          |
|-------------------------|-----------------------------------------------------------------------|-------------------|
| `/oauth2.0/accessToken` | `grant_type=client_credentials&client_id=client&client_secret=secret` | The access token. |

Because there is no `redirect_uri` specified by this grant type, the service identifier recognized by CAS and 
matched in the service registry is taken as the `client_id` instead. You may optionally also pass 
along a `service` or `X-service` header value that identifies the target application url. The header value must 
match the OAuth service definition in the registry that is linked to the client id.
