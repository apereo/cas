# REST Google Authenticator Registration

Source: https://apereo.github.io/cas/development/mfa/GoogleAuthenticator-Authentication-Registration-Rest.html

Registration records may also be passed along to a REST endpoint. The behavior is only activated when an endpoint url is provided.

| Method | Headers | Expected Response | Behavior |
| --- | --- | --- | --- |
| `GET` | `username` | `200`. Account records in the body for the user, or `404` when the user has none. | Fetch user records |
| `GET` | `id` | `200`. Account record in the body for the user. | Fetch record for the given identifier. |
| `GET` | `id`, `username` | `200`. Account record in the body for the user. | Fetch user record for the given identifier. |
| `GET` | N/A | `200`. Account records currently registered. | Fetch all records |
| `DELETE` | N/A | `200` | Delete all records. |
| `DELETE` | `username` | `200`. Count deleted records. | Delete all records assigned to user |
| `DELETE` | `id` | `200` | Delete all records assigned to the given identifier. |
| `POST` | `id`, `username`, `validationCode`, `secretKey`, `scratchCodes`, `name`, `properties`, `tenant`, `lastUsedDateTime` | `200`. `true/false` in the body. | Save user record; `id` identifies the device to update, `properties` is comma-separated, and `tenant` and `lastUsedDateTime` are sent only when set. |

When fetching or counting records for a user, any response other than `200` or `404`, or a body that cannot be parsed, fails the request rather than being treated as “no records”, since a user without records is asked to register a new device.

A sample payload that lists device registration records for the user might be:

```json
[
    "java.util.ArrayList", [{
        "@class": "org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount",
        "scratchCodes": ["java.util.ArrayList", [14883628,81852839,40126334,86724930,54355266] ],
        "id": 123456,
        "secretKey": "UM6ALPJU34CBNFTBBLRFMKBNANMFAIBW",
        "validationCode": 565889,
        "username": "casuser",
        "name": "required-account-name",
        "registrationDate": "2018-06-20T09:47:31.761155Z"
    }]
]
```

The following endpoints need also be available:

| Method | Endpoint | Headers | Expected Response | Behavior |
| --- | --- | --- | --- | --- |
| `GET` | `count` | N/A | `200`. Numeric count | Count all records |
| `GET` | `count` | `username` | `200`. Numeric count, or `404` when the user has none | Count all records for the user |

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.gauth.rest.token-url` (Required; type `String`): Endpoint url of the REST resource used for tokens that are kept to prevent replay attacks.
- `cas.authn.mfa.gauth.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.gauth.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.gauth.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.gauth.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.gauth.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
