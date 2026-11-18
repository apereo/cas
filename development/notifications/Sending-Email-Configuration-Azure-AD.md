# Sending Email - Microsoft Entra ID

Source: https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration-Azure-AD.html

When using Microsoft’s SMTP service with OAuth2 (instead of a static password), this module allows CAS to obtain an access token dynamically instead of storing and using a fixed password. The access token is acquired from Microsoft’s Identity platform using the XOAUTH2 authentication mechanism.

Support is enabled by including the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-mail-microsoft"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.email-provider.microsoft.client-id` (Required; type `String`): Client id used to authenticate to the service.
- `cas.email-provider.microsoft.client-secret` (Required; type `String`): Client secret used to authenticate to the service.
- `cas.email-provider.microsoft.tenant-id` (Required; type `String`): Tenant id used to authenticate to the service. The ultimate authority is built based on the tenant id as `https://login.microsoftonline.com/{tenantId}`.
- `cas.email-provider.microsoft.scopes` (Optional; type `Set<String>`): Scope used to authenticate to the service.
