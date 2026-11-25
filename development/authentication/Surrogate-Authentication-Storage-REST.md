# REST Surrogate Authentication

Source: https://apereo.github.io/cas/development/authentication/Surrogate-Authentication-Storage-REST.html

Decide who may impersonate whom by calling a REST API that answers whether a user may log in as another account and lists the accounts allowed.

REST support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-surrogate-authentication-rest"
}
```

| Method | Description | Parameter(s) | Response |
| --- | --- | --- | --- |
| `GET` | Whether principal can authenticate as a surrogate account. | `surrogate`, `principal` | `202` |
| `GET` | List of accounts principal is eligible to impersonate. | `principal` | JSON list of usernames. |

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.surrogate.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.surrogate.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.surrogate.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.surrogate.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.surrogate.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.surrogate.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
