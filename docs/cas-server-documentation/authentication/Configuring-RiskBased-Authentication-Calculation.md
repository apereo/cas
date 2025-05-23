---
layout: default
title: CAS - Adaptive Risk-based Authentication
category: Authentication
---
{% include variables.html %}

# Risk Calculation

One or more risk calculators may be enabled to allow an analysis of authentication requests.

A high-level explanation of the risk calculation strategy follows:

- If there is no recorded event at all present for the principal, consider the request suspicious.
- If the number of recorded events for the principal based on the active criteria matches the total number of events, consider the
request safe.

{% tabs adaptiveriskcalc %}

{% tab adaptiveriskcalc <i class="fa fa-computer px-1"></i> IP Address %}

This calculator looks into past authentication events that match the client ip address. It is applicable if you wish
to consider authentication requests from unknown ip addresses suspicious for the user. The story here is:

> Find all past authentication events that match the current client ip address and calculate an averaged score.

{% include_cached casproperties.html properties="cas.authn.adaptive.risk.ip" %}

{% endtab %}

{% tab adaptiveriskcalc <i class="fa-brands fa-chrome px-1"></i>Browser User Agent %}

This calculator looks into past authentication events that match the client's `user-agent` string. It is applicable if you wish
to consider authentication requests from unknown browsers suspicious for the user. The story here is:

> Find all past authentication events that match the current client browser and calculate an averaged score.

{% include_cached casproperties.html properties="cas.authn.adaptive.risk.agent" %}

{% endtab %}

{% tab adaptiveriskcalc <i class="fa fa-globe px-1"></i>Geolocation %}

This calculator looks into past authentication events that contain geolocation data, and compares those with the current geolocation.
If current geolocation data is unavailable, it will attempt to geocode the location based on the current client ip address. This feature
mostly depends on whether or not geodata is made available to CAS via the client browser and
requires [geotracking of authentication requests](GeoTracking-Authentication-Requests.html).

The story here is:

> Find all past authentication events that match the current client location and calculate an average score.

{% include_cached casproperties.html properties="cas.authn.adaptive.risk.geo-location" %}

{% endtab %}

{% tab adaptiveriskcalc <i class="fa fa-clock px-1"></i>Date/Time %}

This calculator looks into past authentication events that fit within the defined time-window. It is applicable if you wish
to consider authentication requests outside that window suspicious for the user. The story here is:

> Find all past authentication events that are established X hours before/after now and calculate an averaged score.

{% include_cached casproperties.html properties="cas.authn.adaptive.risk.date-time" %}

{% endtab %}

{% tab adaptiveriskcalc Device Fingerprint %}

This calculator looks into past authentication events that match the device fingerprint. The device (browser) fingerprint 
is calculated via client-side JavaScript and collected during authentication events.

{% include_cached casproperties.html properties="cas.authn.adaptive.risk.device-fingerprint" %}

{% endtab %}

{% endtabs %}
