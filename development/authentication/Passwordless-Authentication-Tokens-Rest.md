# REST Passwordless Authentication Tokens

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Tokens-Rest.html

This strategy allows one design REST endpoints in charge of managing tokens and their expiration policy entirely. CAS continues to generate tokens and the endpoint is only acting as a facade to the real token store, receiving tokens from CAS in an encrypted fashion.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.passwordless.tokens.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.passwordless.tokens.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.passwordless.tokens.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.passwordless.tokens.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.passwordless.tokens.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.passwordless.tokens.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

The following operations need to be supported by the endpoint:

| HTTP Method | Description | Parameter(s) | Response |
| --- | --- | --- | --- |
| `GET` | Locate tokens for the user. | `username` | Token in the response body. |
| `DELETE` | Delete all tokens for the user. | `username` | N/A |
| `DELETE` | Delete a single token for the user. | `username`, `token` | N/A |
| `POST` | Save a token for the user. | `username`, `token` | N/A |
