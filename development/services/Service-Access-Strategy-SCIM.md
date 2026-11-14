# Service Access Strategy - SCIM

Source: https://apereo.github.io/cas/development/services/Service-Access-Strategy-SCIM.html

The SCIM access strategy is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-scim"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.scim.oauth-token` (Required; type `String`): Authenticate into the SCIM server/service via a pre-generated OAuth token.
- `cas.scim.password` (Required; type `String`): Authenticate into the SCIM server with a pre-defined password.
- `cas.scim.schema-mappings` (Required; type `Map<String,String>`): Map of attributes that define how SCIM schema attributes should be populated using CAS attributes. The key is the SCIM schema attribute name and the value is the CAS attribute name. If the CAS attribute is not found, the SCIM attribute is typically ignored.
- `cas.scim.target` (Required; type `String`): The SCIM provisioning target URI.
- `cas.scim.username` (Required; type `String`): Authenticate into the SCIM server with a pre-defined username.
- `cas.scim.asynchronous` (Optional; type `Boolean`; default `false`): Indicate whether provisioning should be asynchronous.
- `cas.scim.enabled` (Optional; type `Boolean`; default `true`): Decide whether scim should be enabled.

This access strategy attempts to locate user groups for the CAS principal by querying for the user and extracting groups from the response. The groups are collected as CAS attributes under a `scimGroups` attribute and then examined against the list of required attributes for service access.

A sample access strategy based follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.scim.v2.access.ScimRegisteredServiceAccessStrategy",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "scimGroups" : [ "java.util.HashSet", [ "..." ] ]
    }
  }
}
```
