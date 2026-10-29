---
layout: default
title: CAS - OpenID Connect Dynamic Registration
category: Protocols
---
{% include variables.html %}

# Dynamic Client Registration - OpenID Connect Authentication
       
Please study [the specification](https://openid.net/specs/openid-connect-registration-1_0.html) 
to learn more about dynamic client registration. Clients may also identify themselves with
[client ID metadata documents](OIDC-Authentication-Client-ID-Metadata-Documents.html), without registering first.

The registration endpoint accepts `POST` requests where the body of the request is to contain the
application registration record. By default, CAS operates in a `PROTECTED` mode where the registration 
endpoint requires user authentication.

{% include_cached casproperties.html properties="cas.authn.oidc.registration" %}
            
The registration endpoint's response on a successful operation will contain information about the registered entity
along with an access token found at `registration_access_token` which can be used to update or retrieve the registered
entity found at `registration_client_uri`.
     
## Protected Dynamic Client Registration

By default, CAS operates in a `PROTECTED` mode where the registration
endpoint requires user authentication via a specially-created *master* access token that contains
the necessary scopes that would allow the registration to proceed. Before you can register clients,
you will need to obtain this access token by sending a `GET` request to the `/oidc/initToken` endpoint.
This endpoint is protected with a Basic authentication scheme and will authenticate the request
using the credentials that are specified in CAS configuration for dynamic registration. The access token
that is received in the response should be passed as a `Bearer` authorization header to all dynamic registration requests.

The registration and client configuration endpoints answer as protected resources do, per
[RFC 6750](https://www.rfc-editor.org/rfc/rfc6750#section-3.1): a request without an access token receives a `401` with a
`WWW-Authenticate: Bearer` challenge, an unknown or expired token a `401` with `invalid_token`, and a token without the
required scope a `403` with `insufficient_scope`. A [DPoP-bound](OIDC-Authentication-DPoP.html) access token is accepted
only with the `DPoP` scheme and its proof, and a certificate-bound token only over a connection that presents its certificate.

## Client Configuration

Application definitions that are registered with CAS dynamically may be retrieved or updated using the `/oidc/clientConfig` endpoint.
This endpoint supports `GET` and `PATCH` requests for read and update operations. Each operation expects a `client_id` parameter
that allows CAS to locate the previously-registered entity for processing. Update requests using `PATCH` may also specify 
the updated registration requests in the request body. Update requests may also update the client's secrets and generate a new one
if the client secret is determined to be expired.
