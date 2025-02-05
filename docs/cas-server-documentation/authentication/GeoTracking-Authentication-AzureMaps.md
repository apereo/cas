---
layout: default
title: CAS - GeoTracking Authentication Requests
category: Authentication
---
{% include variables.html %}

# GeoTracking Authentication - Azure Maps

The Azure Maps support is provided by CAS to support location-aware requests. The Azure Maps Java SDK contains APIs 
that support operations such as searching for an address, obtaining the geo-location of a specific IP address and more.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geolocation-azure" %}

{% include_cached casproperties.html properties="cas.geo-location.azure" %}

By default, Microsoft Entra ID token authentication depends on correct configuration of the following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.
