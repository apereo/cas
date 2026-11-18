# REST Device Storage - Multifactor Authentication Trusted Device/Browser

Source: https://apereo.github.io/cas/development/mfa/Multifactor-TrustedDevice-Authentication-Storage-Rest.html

If you wish to completely delegate the management, verification and persistence of user decisions, you may design a REST API which CAS shall contact to verify user decisions and remember those for later.

Support is provided via the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-trusted-mfa-rest"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.trusted.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.trusted.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.trusted.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.trusted.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.trusted.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

## Retrieve Trusted Records

A `GET` request that returns all trusted authentication records that are valid and not-expired.

```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET "${endpointUrl}/[principal|date]"
```

Response payload may produce a collection of objects that contain:

```json
[
    {
      "principal": "casuser",
      "deviceFingerprint": "...",
      "recordDate": "YYYY-MM-dd",  
      "expirationDate":  "YYYY-MM-dd", 
      "name": "Office",
      "recordKey": "..."
    }
]
```

## Store Trusted Records

A `POST` request that stores a newly trusted device record.

```bash
curl -H "Content-Type: application/json" -X POST -d '${json}' ${endpointUrl}
```

`POST` data will match the following block:

```json
{
    "principal": "...",
    "deviceFingerprint": "...",
    "recordDate": "...",    
    "expirationDate":  "YYYY-MM-dd", 
    "name": "...",
    "recordKey": "..."
}
```

Response payload shall produce a `200` http status code to indicate a successful operation.
