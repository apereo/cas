---
layout: default
title: CAS - GeoTracking Authentication Requests
description: "Map authentication requests to physical locations with GeoLocation providers such as MaxMind or Google Maps."
category: Authentication
---
{% include variables.html %}


# GeoTracking Authentication Requests

Authentication requests can be mapped and translated to physical locations.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation" %}

## GeoLocation Providers

The following geolocation providers are supported by CAS:

| Storage        | Description                                                      |
|----------------|------------------------------------------------------------------|
| Google Maps    | [See this guide](GeoTracking-Authentication-GoogleMaps.html).    |
| Maxmind        | [See this guide](GeoTracking-Authentication-Maxmind.html).       |
| IP GeoLocation | [See this guide](GeoTracking-Authentication-IPGeoLocation.html). |
| Groovy         | [See this guide](GeoTracking-Authentication-Groovy.html).        |
