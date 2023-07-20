---
layout: default
title: CAS - GeoTracking Authentication Requests
category: Authentication
---
{% include variables.html %}


# GeoTracking Authentication - Google Maps

Uses the [Google Maps Geocoding API](https://developers.google.com/maps/documentation/geocoding/start) to translate
authentication requests into a geo-location.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-googlemaps" %}

{% include_cached casproperties.html properties="cas.geo-location.google-maps" %}
