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

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.device-fingerprint.core" %}
   
## Strategies

Device fingerprint can be calculated using the following ways:

- Client IP address
- Randomly generated cookie plus the client IP (default)
- [GeoLocation address](../authentication/GeoTracking-Authentication-Requests.html). You do need to ensure CAS is 
allowed to [ask and process geodata](../authentication/Configuring-Authentication-Events.html) provided by the browser.
- User-agent header
- Browser fingerprint calculated via client-side JavaScript and collected during authentication attempts.

{% tabs devicefingerprint %}

{% tab devicefingerprint <i class="fa fa-computer px-1"></i> Client IP %}

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.device-fingerprint.client-ip" %}

{% endtab %}

{% tab devicefingerprint Cookie <i class="fa fa-cookie-bite px-1"></i> %}

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.device-fingerprint.cookie" %}

{% endtab %}

{% tab devicefingerprint User Agent %}

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.device-fingerprint.user-agent" %}

{% endtab %}

{% tab devicefingerprint <i class="fa-brands fa-chrome px-1"></i>Browser %}

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.device-fingerprint.browser" %}

{% endtab %}

{% tab devicefingerprint <i class="fa fa-globe px-1"></i>GeoLocation %}

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.device-fingerprint.geolocation" %}

{% endtab %}

{% endtabs %}


