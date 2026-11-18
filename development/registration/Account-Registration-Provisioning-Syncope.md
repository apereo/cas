# Account (Self-Service) Registration - Apache Syncope Provisioning

Source: https://apereo.github.io/cas/development/registration/Account-Registration-Provisioning-Syncope.html

Create the accounts users register through CAS in Apache Syncope.

Account registration requests can be submitted to Apache Syncope. Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-syncope-authentication"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.account-registration.provisioning.syncope.basic-auth-password` (Required; type `String`): Specify the password for REST authentication.
- `cas.account-registration.provisioning.syncope.basic-auth-username` (Required; type `String`): Specify the username for REST authentication.
- `cas.account-registration.provisioning.syncope.domain` (Required; type `String`; default `Master`): Syncope domain used for authentication, etc. Multiple domains can be separated via comma. Each domain entry results in a separate authentication attempt and transaction by CAS.
- `cas.account-registration.provisioning.syncope.realm` (Required; type `String`; default `/`): Syncope realm used for user provisioning. Realms define a hierarchical security domain tree, primarily meant for containing users. The root realm contains everything, and other realms can be seen as containers that split up the total number of entities into smaller pools.
- `cas.account-registration.provisioning.syncope.url` (Required; type `String`): Syncope instance URL primary used for REST.
- `cas.account-registration.provisioning.syncope.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
