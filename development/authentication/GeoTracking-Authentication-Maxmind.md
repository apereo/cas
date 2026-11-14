# GeoTracking Authentication - Maxmind

Source: https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Maxmind.html

Uses [Maxmind](https://github.com/maxmind) to translate authentication requests into a geo-location.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-geolocation-maxmind"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.geo-location.maxmind.city-database` (Required; type `Resource`): Path to the location of the database file containing cities.
- `cas.geo-location.maxmind.country-database` (Required; type `Resource`): Path to the location of the database file containing countries.
- `cas.geo-location.maxmind.account-id` (Optional; type `Integer`; default `0`): Geolocating an IP address using Maxmind web services will need your MaxMind account ID and license key. A zero or negative value will deactivate the web services integration.
- `cas.geo-location.maxmind.license-key` (Optional; type `String`): Geolocating an IP address using Maxmind web services will need your MaxMind account ID and license key. A blank, undefined value will deactivate the web services integration.
