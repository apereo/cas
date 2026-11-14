# GeoTracking Authentication - Azure Maps

Source: https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-AzureMaps.html

The Azure Maps support is provided by CAS to support location-aware requests. The Azure Maps Java SDK contains APIs that support operations such as searching for an address, obtaining the geo-location of a specific IP address and more.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-geolocation-azure"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.geo-location.azure.api-key` (Optional; type `String`): API key used for shared key authentication - This is a simple and secure way for authenticating to the Azure Maps services.
- `cas.geo-location.azure.client-id` (Optional; type `String`): Azure maps client id.
- `cas.geo-location.azure.tenant-id` (Optional; type `String`): Directory (tenant) ID.

By default, Microsoft Entra ID token authentication depends on correct configuration of the following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.
