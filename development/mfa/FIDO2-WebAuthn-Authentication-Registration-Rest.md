# REST FIDO2 WebAuthn Multifactor Registration

Source: https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication-Registration-Rest.html

Keep FIDO2 WebAuthn device registrations behind a REST API you provide, which CAS calls to read, save and remove them.

Device registrations may be managed using an external REST API by including the following module in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-webauthn-rest"
}
```

The following parameters are passed:

| Operation | Parameters | Description | Result |
| --- | --- | --- | --- |
| `GET` | N/A | Retrieve all records. | `200` status code; Collection of JSON records in the body. |
| `GET` | `username` | Retrieve all records for user. | `200` status code Collection of JSON records in the body. |
| `POST` | Collection of records as JSON body | Store/Update registered devices. | `200`. |

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.web-authn.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.web-authn.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.web-authn.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.web-authn.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.web-authn.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.web-authn.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
