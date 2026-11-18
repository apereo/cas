# REST YubiKey Registration

Source: https://apereo.github.io/cas/development/mfa/YubiKey-Authentication-Registration-Rest.html

Registration records may be tracked using an external REST API.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.yubikey.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.yubikey.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.yubikey.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.yubikey.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.yubikey.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.yubikey.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

The following endpoints are expected to be available and implemented by the REST API:

| METHOD | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/` | Get all registered records. |
| `GET` | `/{user}` | Get all registered records for the user. |
| `DELETE` | `/` | Delete all registered records. |
| `DELETE` | `/{user}` | Delete all registered records for the user. |
| `DELETE` | `/{user}/{id}` | Delete the registered device by its id from the registration record for the user. |
| `POST` | `/` | Store registration records passed as the request body. |
