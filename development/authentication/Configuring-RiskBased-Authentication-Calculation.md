# Risk Calculation

Source: https://apereo.github.io/cas/development/authentication/Configuring-RiskBased-Authentication-Calculation.html

One or more risk calculators may be enabled to allow an analysis of authentication requests.

A high-level explanation of the risk calculation strategy follows:

- If there is no recorded event at all present for the principal, consider the request suspicious.
- If the number of recorded events for the principal based on the active criteria matches the total number of events, consider the request safe.

**IP Address**

This calculator looks into past authentication events that match the client ip address. It is applicable if you wish to consider authentication requests from unknown ip addresses suspicious for the user. The story here is:

> Find all past authentication events that match the current client ip address and calculate an averaged score.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.risk.ip.enabled` (Optional; type `Boolean`; default `false`): Enable IP address checking and criteria to calculate risky authentication attempts.

**Browser User Agent**

This calculator looks into past authentication events that match the client’s `user-agent` string. It is applicable if you wish to consider authentication requests from unknown browsers suspicious for the user. The story here is:

> Find all past authentication events that match the current client browser and calculate an averaged score.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.risk.agent.enabled` (Optional; type `Boolean`; default `false`): Enable user-agent checking and criteria to calculate risky authentication attempts.

**Geolocation**

This calculator looks into past authentication events that contain geolocation data, and compares those with the current geolocation. If current geolocation data is unavailable, it will attempt to geocode the location based on the current client ip address. This feature mostly depends on whether or not geodata is made available to CAS via the client browser and requires [geotracking of authentication requests](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Requests.html).

The story here is:

> Find all past authentication events that match the current client location and calculate an average score.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.risk.geo-location.enabled` (Optional; type `Boolean`; default `false`): Enable geolocation checking and criteria to calculate risky authentication attempts.

**Date/Time**

This calculator looks into past authentication events that fit within the defined time-window. It is applicable if you wish to consider authentication requests outside that window suspicious for the user. The story here is:

> Find all past authentication events that are established X hours before/after now and calculate an averaged score.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.risk.date-time.enabled` (Optional; type `Boolean`; default `false`): Enable date/time checking and criteria to calculate risky authentication attempts.
- `cas.authn.adaptive.risk.date-time.window-in-hours` (Optional; type `Integer`; default `2`): The hourly window used before and after each authentication event in calculation to establish a pattern that can then be compared against the threshold.

**Device Fingerprint**

This calculator looks into past authentication events that match the device fingerprint. The device (browser) fingerprint is calculated via client-side JavaScript and collected during authentication events.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.risk.device-fingerprint.enabled` (Optional; type `Boolean`; default `false`): Enable date/time checking and criteria to calculate risky authentication attempts.
