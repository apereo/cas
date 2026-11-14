# SCIM Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-SCIM.html

The following configuration describes how to fetch and retrieve attributes from SCIM servers.

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

- `cas.authn.attribute-repository.scim[0].oauth-token` (Required; type `String`): Authenticate into the SCIM server/service via a pre-generated OAuth token.
- `cas.authn.attribute-repository.scim[0].password` (Required; type `String`): Authenticate into the SCIM server with a pre-defined password.
- `cas.authn.attribute-repository.scim[0].target` (Required; type `String`): The SCIM provisioning target URI.
- `cas.authn.attribute-repository.scim[0].username` (Required; type `String`): Authenticate into the SCIM server with a pre-defined username.
- `cas.authn.attribute-repository.scim` (Optional; type `List<ScimPrincipalAttributesProperties>`): Retrieve attributes from SCIM servers.
- `cas.authn.attribute-repository.scim[0].enabled` (Optional; type `boolean`; default `true`): Decide whether scim should be enabled.
- `cas.authn.attribute-repository.scim[0].id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.scim[0].order` (Optional; type `int`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.scim[0].state` (Optional; type `AttributeRepositoryStates`; default `ACTIVE`): Whether attribute resolution based on this source is enabled. Available values are as follows: - `ACTIVE`: Active and enabled repository, and is invoked by default automatically. - `DISABLED`: Attribute repository is disabled and will not be used to resolve people and attributes. - `STANDBY`: Repository is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for and will not be registered into the resolution plan.

## Mapping Attributes

The following attributes are extracted from the SCIM response as CAS attributes:

| Attribute |
| --- |
| `scimUserAddress0Country` |
| `scimUserAddress0Display` |
| `scimUserAddress0Formatted` |
| `scimUserAddress0Locality` |
| `scimUserAddress0PostalCode` |
| `scimUserAddress0Ref` |
| `scimUserAddress0Region` |
| `scimUserAddress0StreetAddress` |
| `scimUserAddress0Type` |
| `scimUserDisplayName` |
| `scimUserExternalId` |
| `scimUserFamilyName` |
| `scimUserFormatted` |
| `scimUserGivenName` |
| `scimUserHonorificPrefix` |
| `scimUserHonorificSuffix` |
| `scimUserMiddleName` |
| `scimUserId` |
| `scimUserLocale` |
| `scimUserNickName` |
| `scimUserPreferredLanguage` |
| `scimUserProfileUrl` |
| `scimUserTimezone` |
| `scimUserTitle` |
| `scimUserName` |
| `scimUserType` |
| `scimUserActive` |
| `scimUserRole0Display` |
| `scimUserRole0Value` |
| `scimUserRole0Ref` |
| `scimUserRole0Type` |
| `scimUserPhone0Display` |
| `scimUserPhone0Value` |
| `scimUserPhone0Ref` |
| `scimUserPhone0Type` |
| `scimUserEmail0Display` |
| `scimUserEmail0Value` |
| `scimUserEmail0Ref` |
| `scimUserEmail0Type` |
| `scimUserGroup0Display` |
| `scimUserGroup0Value` |
| `scimUserGroup0Ref` |
| `scimUserGroup0Type` |
| `scimUserIm0Display` |
| `scimUserIm0Value` |
| `scimUserIm0Ref` |
| `scimUserIm0Type` |
| `scimUserEntitlement0Display` |
| `scimUserEntitlement0Value` |
| `scimUserEntitlement0Ref` |
| `scimUserEntitlement0Type` |
| `scimUserEnterpriseCostCenter` |
| `scimUserEnterpriseDepartment` |
| `scimUserEnterpriseDivision` |
| `scimUserEnterpriseEmployeeNumber` |
| `scimUserEnterpriseManager` |
| `scimUserEnterpriseOrganization` |

Note that schema attributes that may contain multiple values such as `roles` or `groups` are collecting using an incrementing index in the attribute name. For example, `scimUserRole0Display`, `scimUserRole1Display`, etc.
