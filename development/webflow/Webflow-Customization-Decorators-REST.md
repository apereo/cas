# REST Decorators - Webflow Decorations

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Decorators-REST.html

RESTful login decorators allow one to inject data into the webflow context by reaching out to an external REST API. If the endpoint responds back with a `200` status code, CAS would parse the response body into a JSON object and will stuff the result into the webflow’s `flowScope` container under the key `decoration`. Please remember that data stuffed into the webflow **MUST** be serializable and if you intend to pass along complex objects types and fancy data structures, you need to make sure they can safely and ultimately transform into a simple `byte[]`.

The following settings and properties are available from the CAS configuration catalog:

- `cas.webflow.login-decorator.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.webflow.login-decorator.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.webflow.login-decorator.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.webflow.login-decorator.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.webflow.login-decorator.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.webflow.login-decorator.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
