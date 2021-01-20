---
layout: default
title: CAS - GeoTracking Authentication Requests
category: Authentication
---
{% include variables.html %}


# GeoTracking Authentication Requests

Authentication requests can be mapped and translated to physical locations.

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation" %}

## Google Maps

Uses the [Google Maps Geocoding API](https://developers.google.com/maps/documentation/geocoding/start) to translate
authentication requests into a geo-location.

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-googlemaps" %}

{% include casproperties.html properties="cas.google-maps" %}

## Maxmind

Uses [Maxmind](https://www.maxmind.com/en/home) to translate
authentication requests into a geo-location.

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-maxmind" %}

{% include casproperties.html properties="cas.maxmind" %}
