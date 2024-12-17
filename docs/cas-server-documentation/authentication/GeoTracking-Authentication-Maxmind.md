---
layout: default
title: CAS - GeoTracking Authentication Requests
category: Authentication
---
{% include variables.html %}


# GeoTracking Authentication - Maxmind

Uses [Maxmind](https://github.com/maxmind) to translate authentication requests into a geo-location.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-maxmind" %}

{% include_cached casproperties.html properties="cas.geo-location.maxmind" %}
