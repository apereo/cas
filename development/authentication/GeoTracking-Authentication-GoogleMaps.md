# GeoTracking Authentication - Google Maps

Source: https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-GoogleMaps.html

Uses the [Google Maps Geocoding API](https://developers.google.com/maps/documentation/geocoding/start) to translate authentication requests into a geo-location.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-geolocation-googlemaps"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.geo-location.google-maps.api-key` (Required; type `String`): Authenticate into google maps via an API key.
- `cas.geo-location.google-maps.client-id` (Required; type `String`): Authenticate into google maps via a client id.
- `cas.geo-location.google-maps.client-secret` (Required; type `String`): Authenticate into google maps via a client secret.
- `cas.geo-location.google-maps.connect-timeout` (Optional; type `String`; default `PT3S`): The connection timeout when reaching out to google maps.
- `cas.geo-location.google-maps.google-apps-engine` (Optional; type `Boolean`; default `false`): When true, a strategy for handling URL requests using Google App Engine's URL Fetch API.
