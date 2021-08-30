---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Device Fingerprint - Multifactor Authentication Trusted Device/Browser

In order to distinguish trusted devices from each other we need to calculate a device fingerprint that uniquely
identifies individual devices. Calculation of this device fingerprint can utilize a combination of multiple components
from the request. 

Device fingerprint can be calculated using the following ways:

- Client IP address
- Randomly generated cookie plus the client IP (default)
- [GeoLocation address](../authentication/GeoTracking-Authentication-Requests.html). You do need to ensure CAS is 
allowed to [ask and process geodata](../authentication/Configuring-Authentication-Events.html) provided by the browser.
- User-agent header

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.device-fingerprint" %}
