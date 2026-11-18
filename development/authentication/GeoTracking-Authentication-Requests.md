# GeoTracking Authentication Requests

Source: https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Requests.html

Authentication requests can be mapped and translated to physical locations.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-geolocation"
}
```

## GeoLocation Providers

The following geolocation providers are supported by CAS:

| Storage | Description |
| --- | --- |
| Google Maps | [See this guide](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-GoogleMaps.html). |
| Maxmind | [See this guide](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Maxmind.html). |
| IP GeoLocation | [See this guide](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-IPGeoLocation.html). |
| Groovy | [See this guide](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Groovy.html). |
