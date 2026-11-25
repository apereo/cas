# SCIM Principal Provisioning

Source: https://apereo.github.io/cas/development/integration/SCIM-Provisioning.html

The SCIM standard is created to simplify user management and provisioning in the cloud by defining a schema for representing users and groups and a REST API for all the necessary CRUD operations. SCIM integrations with CAS allow deployers to auto-provision the authenticated CAS principal to a SCIM server/target that supports the v2 specification of the SCIM protocol with additional support to map principal attributes into the appropriate claims and properties of the user resource.

Typical use case for enabling SCIM is to synchronize and provision user accounts, just in time, to services and applications that are integrated with CAS for single sign-on. In cases where the application also has its own account store, a mapping of user accounts between the CAS canonical account store (LDAP, JDBC, etc) and the application may be required. To accommodate this issue, CAS may be allowed to provision the authenticated principal via SCIM to a provisioning/identity/entity engine which would then dynamically synchronize user profiles to target systems.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

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

## Mapping Attributes

SCIM user resources are populated from CAS authenticated principals using one-to-one mapping rules. For example, the `givenName` attribute in the SCIM schema is mapped and populated from the `givenName` attributes found for the CAS principal.

The set of attributes that are mapped are as follows:

| Attribute | Description |
| --- | --- |
| `userName` | Set to the principal id. |
| `password` | Set to the credential password, if available. |
| `nickName` | Set to the principal attribute `nickName`. |
| `displayName` | Set to the principal attribute `displayName`. |
| `givenName` | Set to the principal attribute `givenName`. |
| `familyName` | Set to the principal attribute `familyName`. |
| `middleName` | Set to the principal attribute `middleName`. |
| `email` | Set to the principal attribute `email`. |
| `phoneNumber` | Set to the principal attribute `phoneNumber`. |
| `externalId` | Set to the principal attribute `externalId`. |
| `entitlements` | Set to the principal attribute `entitlements`. |
| `roles` | Set to the principal attribute `roles`. |
| `addresses` | Set to the principal attribute `addresses`. |
| `ims` | Set to the principal attribute `ims`. |
| `employeeNumber` | Set to the principal attribute `employeeNumber`. |
| `costCenter` | Set to the principal attribute `costCenter`. |
| `division` | Set to the principal attribute `division`. |
| `department` | Set to the principal attribute `department`. |
| `organization` | Set to the principal attribute `organization`. |
| `resourceType` | Set to the principal attribute `resourceType`. |
| `groups` | Set to the principal attribute `groups`. |

The default mapping rules can be controlled and customized using CAS properties.

If the default mapping rules are not suitable, the mapping rules can always be adjusted and customized using the following bean:

```java
@Bean
public ScimPrincipalAttributeMapper scim2PrincipalAttributeMapper() {
    return new MyPrincipalAttributeMapper();
}
```

## Mapping Groups

CAS can also provision groups to SCIM targets. The groups are populated from the principal attribute `groups`. Memberships can be dynamically created, updated or removed based on the principal’s group memberships.

## Per Application

SCIM relevant settings can be specified per application in form of service properties.

The following properties are available and recognized by CAS for various modules and features:

| Name | Default Value | Type | Group |
| --- | --- | --- | --- |
| `scimOAuthToken` |  | `STRING` | `SCIM` |
| `scimUsername` |  | `STRING` | `SCIM` |
| `scimPassword` |  | `STRING` | `SCIM` |
| `scimTarget` |  | `STRING` | `SCIM` |

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 1,
  "properties" : {
    "@class" : "java.util.HashMap",
    "scimUsername" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "username" ] ]
    },
    "scimPassword" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "p@$$w0rd" ] ]
    }
  }
}
```
