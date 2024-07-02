---
layout: default
title: CAS - GeoTracking Authentication Requests
category: Authentication
---
{% include variables.html %}


# GeoTracking Authentication - Groovy

Use a Groovy script to translate authentication requests into a geo-location.

{% include_cached casproperties.html properties="cas.geo-location.groovy" %}

The outline of the script may be as follows:

```groovy
import org.apereo.cas.authentication.adaptive.geo.*
import org.springframework.context.*

GeoLocationResponse locateByAddress(Object... args) {
    def (address,appContext,logger) = args
    logger.info("Requesting info on ${address.hostAddress}")
    return GeoLocationResponse
            .builder()
            .latitude(38)
            .longitude(-77)
            .build()
            .addAddress("USA")
}

GeoLocationResponse locateByCoordinates(Object... args) {
    def (latitude,longitude,appContext,logger) = args
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


To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
