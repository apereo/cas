# REST - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-REST.html

Contact a REST endpoint via `POST` to detect authentication policy. The message body contains the CAS authenticated principal that can be used to examine account status and policy.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.rest[0].url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.policy.rest` (Optional; type `List<RestAuthenticationPolicyProperties>`): Execute a rest endpoint to detect authentication policy.
- `cas.authn.policy.rest[0].basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.policy.rest[0].basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.policy.rest[0].headers` (Optional; type `Map<String, String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.policy.rest[0].maximum-retry-attempts` (Optional; type `int`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.

Response codes from the REST endpoint are translated as such:

| Code | Result |
| --- | --- |
| `200` | Successful authentication. |
| `403`, `405` | Produces a `AccountDisabledException` |
| `404` | Produces a `AccountNotFoundException` |
| `423` | Produces a `AccountLockedException` |
| `412` | Produces a `AccountExpiredException` |
| `428` | Produces a `AccountPasswordMustChangeException` |
| Other | Produces a `FailedLoginException` |
