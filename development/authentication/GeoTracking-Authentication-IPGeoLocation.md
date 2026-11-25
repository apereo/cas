# GeoTracking Authentication - IP GeoLocation

Source: https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-IPGeoLocation.html

Uses the [IP GeoLocation API](https://ipgeolocation.io/) to translate authentication requests into a geo-location.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-geolocation-ip"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.geo-location.ip-geo-location.api-key` (Required; type `String`): API key required for this integration.
