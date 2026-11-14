# Apache Syncope - Principal Provisioning

Source: https://apereo.github.io/cas/development/integration/Syncope-Provisioning.html

CAS may be allowed to provision the authenticated principal via Apache Syncope to a provisioning/identity/entity engine which would then dynamically synchronize user profiles to target systems. Successful login attempts would allow CAS to pass back the authenticated user profile to Apache Syncope, which would then be tasked to create and provision the user to appropriate target systems.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

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

- `cas.authn.syncope.provisioning.basic-auth-password` (Required; type `String`): Specify the password for REST authentication.
- `cas.authn.syncope.provisioning.basic-auth-username` (Required; type `String`): Specify the username for REST authentication.
- `cas.authn.syncope.provisioning.domain` (Required; type `String`; default `Master`): Syncope domain used for authentication, etc. Multiple domains can be separated via comma. Each domain entry results in a separate authentication attempt and transaction by CAS.
- `cas.authn.syncope.provisioning.realm` (Required; type `String`; default `/`): Syncope realm used for principal provisioning. Realms define a hierarchical security domain tree, primarily meant for containing users. The root realm contains everything, and other realms can be seen as containers that split up the total number of entities into smaller pools.
- `cas.authn.syncope.provisioning.search-filter` (Required; type `String`; default `username=={user}`): User FIQL filter to use for searching. Syntax is `username=={user}` or `username=={0}`.
- `cas.authn.syncope.provisioning.url` (Required; type `String`): Syncope instance URL primary used for REST.
- `cas.authn.syncope.provisioning.attribute-mappings` (Optional; type `Map<String,String>`): Map of attributes that optionally may be used to control the names of the collected attributes from Syncope. If an attribute is provided by Syncope, it can be listed here as the key of the map with a value that should be the name of that attribute as collected and recorded by CAS. For example, the convention `lastLoginDate->lastDate` will process the Syncope attribute `lastLoginDate` and will internally rename that to `lastDate`. If no mapping is specified, CAS defaults will be used instead.
- `cas.authn.syncope.provisioning.enabled` (Optional; type `Boolean`; default `false`): Whether or not provisioning should be enabled with Syncope.
- `cas.authn.syncope.provisioning.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
