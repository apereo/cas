# Microsoft Entra ID Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-AzureAD.html

The following configuration describes how to fetch and retrieve attributes from Microsoft Entra ID attribute repositories.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-azuread-authentication"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.azure-active-directory[0].client-id` (Required; type `String`): Client id of the registered app in microsoft azure portal.
- `cas.authn.attribute-repository.azure-active-directory[0].client-secret` (Required; type `String`): Client secret of the registered app in microsoft azure portal.
- `cas.authn.attribute-repository.azure-active-directory` (Optional; type `List<AzureActiveDirectoryAttributesProperties>`): Retrieve attributes from multiple Microsoft Graph instances.
- `cas.authn.attribute-repository.azure-active-directory[0].api-base-url` (Optional; type `String`): Base API url used to contact microsoft graph for calls.
- `cas.authn.attribute-repository.azure-active-directory[0].attributes` (Optional; type `String`): Comma-separated attributes and user properties to fetch from microsoft graph. If attributes are specified here, they would be the only ones requested and fetched. If this field is left blank, a default set of attributes are fetched and returned.
- `cas.authn.attribute-repository.azure-active-directory[0].case-insensitive` (Optional; type `boolean`): Whether attribute repository should consider the underlying attribute names in a case-insensitive manner.
- `cas.authn.attribute-repository.azure-active-directory[0].domain` (Optional; type `String`): Domain that is appended to usernames when doing lookups. The `@` is automatically included.
- `cas.authn.attribute-repository.azure-active-directory[0].grant-type` (Optional; type `String`): Grant type used to fetch access tokens; defaults to `client_credentials`.
- `cas.authn.attribute-repository.azure-active-directory[0].id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.azure-active-directory[0].logging-level` (Optional; type `String`): Adjust the logging level of the API calls. Defaults to `basic`. Accepted values are `none,headers,basic,body`.
- `cas.authn.attribute-repository.azure-active-directory[0].login-base-url` (Optional; type `String`): Base login url used to fetch access tokens.
- `cas.authn.attribute-repository.azure-active-directory[0].order` (Optional; type `int`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.azure-active-directory[0].resource` (Optional; type `String`): Resource to fetch access tokens for; defaults to the graph api url.
- `cas.authn.attribute-repository.azure-active-directory[0].scope` (Optional; type `String`): Scope used when fetching access tokens.
- `cas.authn.attribute-repository.azure-active-directory[0].tenant` (Optional; type `String`): The microsoft tenant id.
