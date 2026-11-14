# User Interface - External Views

Source: https://apereo.github.io/cas/development/ux/User-Interface-Views-External.html

Views also may be externalized outside the web application conditionally and individually, provided the external path via CAS settings. If a view template file is not found at the externalized path, the default one that ships with CAS will be used as the fallback.

Views may also be found using an external URL in CAS settings that is responsible to produce the full view body in the response. This URL endpoint will receive the available request headers as well as the following headers in its request:

| Header |
| --- |
| `owner` |
| `template` |
| `resource` |
| `theme`, if available |
| `locale`, if available |

Upon a successful `200` status result, the response body is expected to contain the view that will be rendered by CAS.

The following settings and properties are available from the CAS configuration catalog:

- `cas.view.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.view.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.view.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.view.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.view.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.view.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
