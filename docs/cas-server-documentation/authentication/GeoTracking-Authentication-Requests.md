---
layout: default
title: CAS - GeoTracking Authentication Requests
category: Authentication
---
{% include variables.html %}


# GeoTracking Authentication Requests

Authentication requests can be mapped and translated to physical locations.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation" %}

## Google Maps

Uses the [Google Maps Geocoding API](https://developers.google.com/maps/documentation/geocoding/start) to translate
authentication requests into a geo-location.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-googlemaps" %}

{% include_cached casproperties.html properties="cas.geo-location.google-maps" %}

## Maxmind

Uses [Maxmind](https://www.maxmind.com/en/geoip2-databases) to translate authentication requests into a geo-location.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-maxmind" %}

{% include_cached casproperties.html properties="cas.geo-location.maxmind" %}

## Groovy

Use a Groovy script to translate authentication requests into a geo-location.

{% include_cached casproperties.html properties="cas.geo-location.groovy" %}

The outline of the script may be as follows:

```groovy
import org.apereo.cas.authentication.adaptive.geo.*
import org.springframework.context.*

GeoLocationResponse locateByAddress(Object... args) {
    def address = args[0] as InetAddress
    def appContext = args[1] as ApplicationContext
    def logger = args[2]
    logger.info("Requesting info on ${address.hostAddress}")
    return GeoLocationResponse
            .builder()
            .latitude(38)
            .longitude(-77)
            .build()
            .addAddress("USA")
}

GeoLocationResponse locateByCoordinates(Object... args) {
    def latitude = args[0]
    def longitude = args[1]
    def appContext = args[2] as ApplicationContext
    def logger = args[3]
    return GeoLocationResponse
            .builder()
            .latitude(latitude)
            .longitude(longitude)
            .build()
            .addAddress("USA")
}
```

The parameters that may be passed are as follows:

| Parameter    | Description                                                                 |
|--------------|-----------------------------------------------------------------------------|
| `address`    | IP address that should be geo-located.                                      |
| `latitude`   | Latitude geographic coordinate measurement.                                 |
| `longitude`  | Longitude geographic coordinate measurement.                                |
| `appContext` | The Spring application context.                                             |
| `logger`     | The object responsible for issuing log messages such as `logger.info(...)`. |


## IP GeoLocation

Uses the [IP GeoLocation API](https://ipgeolocation.io/) to translate authentication requests into a geo-location.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-ip" %}

{% include_cached casproperties.html properties="cas.geo-location.ip-geo-location" %}
