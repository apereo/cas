---
layout: default
title: CAS - GeoTracking Authentication Requests
category: Authentication
---

# GeoTracking Authentication Requests

Authentication requests can be mapped and translated to physical locations.

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation" %}

## Google Maps

Uses the [Google Maps Geocoding API](https://developers.google.com/maps/documentation/geocoding/start) to translate
authentication requests into a geo-location.

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-googlemaps" %}

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#googlemaps-geotracking).

## Maxmind

Uses [Maxmind](https://www.maxmind.com/en/home) to translate
authentication requests into a geo-location.

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-maxmind" %}

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#maxmind-geotracking).
