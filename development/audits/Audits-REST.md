# REST Audits

Source: https://apereo.github.io/cas/development/audits/Audits-REST.html

Audit events may also be `POST`ed to an endpoint of your choosing. To activate this feature, enable the following module in your configuration:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-audit-rest"
}
```

The body of the HTTP request is a JSON representation of the audit record.

The following settings and properties are available from the CAS configuration catalog:

- `cas.audit.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.audit.rest.asynchronous` (Optional; type `Boolean`; default `true`): Make storage requests asynchronously.
- `cas.audit.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.audit.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.audit.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.audit.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.audit.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
