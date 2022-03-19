---
layout: default
title: CAS - OpenID Connect Dynamic Registration
category: Protocols
---
{% include variables.html %}

# Client Dynamic Registration - OpenID Connect Authentication
       
Please study [the specification](https://openid.net/specs/openid-connect-registration-1_0.html) 
to learn more about dynamic client registration.

The registration endpoint accepts `POST` requests where the body of the request is to contain the
application registration record. By default, CAS operates in a `PROTECTED` mode where the registration 
endpoint requires user authentication.

{% include_cached casproperties.html properties="cas.authn.oidc.registration" %}
            
The registration endpoint's response on a successful operation will contain information about the registered entity
along with an access token found at `registration_access_token` which can be used to update or retrieve the registered
entity found at `registration_client_uri`.
                                            
## Client Configuration

Application definitions that are registered with CAS dynamically may be retrieved or updated using the `/oidc/clientConfig` endpoint.
This endpoint supports `GET` and `PATCH` requests for read and update operations. Each operation expects a `clientId` parameter
that allows CAS to locate the previously-registered entity for processing. Update requests using `PATCH` may also specify 
the updated registration requests in the request body. Update requests may also update the client's secrets and generate a new one
if the client secret is determined to be expired.
