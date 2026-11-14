# REST - Attribute Consent Storage

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Consent-Storage-REST.html

Support is enabled by including the following module in the WAR Overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-consent-rest"
}
```

Endpoints must be designed to accept/process `application/json`.

| Operation | Method | Data | Expected Response |
| --- | --- | --- | --- |
| Locate consent decision | `GET` | `service`, `principal` as query parameters. | `200`. The consent decision object in the body. |
| Locate consent decision for user | `GET` | `principal` as query parameter. | `200`. The consent decisions object in the body. |
| Locate all consent decisions | `GET` | N/A | `200`. The consent decisions object in the body. |
| Store consent decision | `POST` | Consent decision object in the body. | `200`. |
| Delete consent decision | `DELETE` | `decisionId` and `principal` as query parameters | `200`. |
| Delete consent decisions | `DELETE` | `principal` as query parameter. | `200`. |

The consent decision object in transit will and must match the JSON structure above.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.consent.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.consent.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.consent.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.consent.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.consent.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.

## Multitenancy

Configuration settings for attribute consent storage can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information.
