# REST Authentication Interrupt

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Interrupt-REST.html

This strategy reaches out to a REST endpoint resource whose job is to dynamically calculate whether the authentication flow should be interrupted given the following parameters:

| Parameter | Description |
| --- | --- |
| `username` | Authenticated principal id. |
| `service` | The identifier (URL) for the requesting application. |
| `registeredService` | The identifier of the registered service matched and found in the registry. |

On a successful operation with a status code of `200`, the response body is expected to contain the JSON payload whose syntax and structure is identical to what is described above. Responses with any other (non-`2xx`) status code are ignored and do not interrupt the authentication flow.

The following settings and properties are available from the CAS configuration catalog:

- `cas.interrupt.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.interrupt.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.interrupt.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.interrupt.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.interrupt.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.interrupt.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
