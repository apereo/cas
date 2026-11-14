# Simple Multifactor Authentication - REST Token Management

Source: https://apereo.github.io/cas/development/mfa/Simple-Multifactor-Authentication-TokenManagement-REST.html

Token validation and management can also be outsources to an external REST API.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.simple.token.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.simple.token.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.simple.token.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.simple.token.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.simple.token.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.simple.token.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

The API service is primarily response for two operations: issuing tokens so they may be shared with the end-user and validating tokens once the end-user provides them back to CAS.

## Generating Tokens

When tokens need to be generated, this API endpoint would be invoked via a `POST` to create the ticket identifier. The body of the request will contain the authenticated principal that is put through the multifactor authentication flow, and the requesting application for which the token should be generated is passed to the API via a `service` parameter.

The endpoint is expected to respond to token generation requests at a `/new` URL path suffix, and should produce a `2xx` status code where the response body is expected to contain the token identifier.

## Storing Tokens

Generated tokens, that are shared with end-users, can be stored via the REST API endpoint using a `POST`. The body of the request would contain the actual token definition and details that should be stored. The API service should produce a `2xx` status code on successful operations.

## Validating Tokens

Generated tokens are passed to this API to validation where the token is appended to the URL endpoint and acts as a path variable. The response that is returned to a `POST` call must be accompanied by a `2xx` status code where the body should contain `id` and `attributes` fields, the latter being optional, which represent the authenticated principal for CAS:

```json
{
  "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
  "id": "casuser",
  "attributes": {
    "@class": "java.util.LinkedHashMap",
    "names": [
      "java.util.List", ["cas", "user"]
    ]
  }
}
```

## Fetching Tokens

Generated tokens are passed to this API to be retrieved and queried where the token is appended to the URL endpoint and acts as a path variable. The response that is returned to a `GET` call must be accompanied by a `2xx` status code where the body should contain `id` and `attributes` fields, the latter being optional, which represent the authenticated principal for CAS:

```json
{
  "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
  "id": "casuser",
  "attributes": {
    "@class": "java.util.LinkedHashMap",
    "names": [
      "java.util.List", ["cas", "user"]
    ]
  }
}
```
