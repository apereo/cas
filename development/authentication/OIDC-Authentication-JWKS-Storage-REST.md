# OpenID Connect Authentication JWKS Storage - REST

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Storage-REST.html

Keystore generation can be outsourced to an external REST API. Endpoints must be designed to accept/process `application/json` and generally should return a `2xx` response status code.

The following requests are made by CAS to the endpoint:

| Operation | Parameters | Description | Result |
| --- | --- | --- | --- |
| `GET` | N/A | Retrieve the keystore, or generate one. | `2xx` status code; JWKS resource in response body. |
| `POST` | JWKS in request body. | Store the keystore. | `2xx` status code. |

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.jwks.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.oidc.jwks.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.oidc.jwks.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.oidc.jwks.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.oidc.jwks.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.oidc.jwks.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
