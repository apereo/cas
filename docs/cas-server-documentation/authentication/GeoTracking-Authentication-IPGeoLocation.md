---
layout: default
title: CAS - GeoTracking Authentication Requests
category: Authentication
---
{% include variables.html %}


# GeoTracking Authentication - IP GeoLocation

Uses the [IP GeoLocation API](https://ipgeolocation.io/) to translate authentication requests into a geo-location.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-ip" %}

{% include_cached casproperties.html properties="cas.geo-location.ip-geo-location" %}
