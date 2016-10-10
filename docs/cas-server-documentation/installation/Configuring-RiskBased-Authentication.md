---
layout: default
title: CAS - Adaptive Risk-based Authentication
---

# Risk-based Authentication

<div class="alert alert-warning"><strong>Achtung, liebe Leser</strong><p>This feature swings more towards the experimental.</p></div>

Risk-based authentication allows CAS to detect suspicious and seemingly-fraudulent authentication requests based on past user behavior
and collected authentication events, statistics, etc. Once *after* primary authentication where the principal is identified,
the authentication transaction is analyzed via a number of configurable criteria and fences to determine how *risky* the attempt may be.
The result of the evaluation step is a comulative risk score that is then weighed against a risk threshold set by the CAS operator.
In the event that the authentication attempt is considered risky well beyond the risk threshold, CAS may be allowed to take action and
mitigate that risk. 

Simply put, the story told is:

> If an authentication request is at least [60%] risky, take action to mitigate that risk. 

The functionality of this feature is **ENTIRELY** dependant upon collected statistics and authentication events in the past.
Without data, there is nothing to analyze and no risk to detect.

Note that evaluation of attempts and mitigation of risks are all recorded in the audit log.

<div class="alert alert-info"><strong>Adaptive Authentication</strong><p>
If you need to preemptively evaluate authentication attempts based on various characterisitcs of the request, 
you may be interested in <a href="Configuring-Adaptive-Authentication.html">this guide</a> instead.</p></div>

## Risk Calculation

One or more risk calculators may be enabled to allow an analysis of authentication requests.

A high level explanation of the risk calculation strategy follows:

- If there is no recorded event at all present for the principal, consider the request suspicious.
- If the number of recorded events for the principal based on the active criteria matches the total number of events, consider the 
request safe.

### IP Address

### Browser User Agent

### Geolocation

### Date/Time

## Risk Mitigation

Once an authentication attempt is deemed risky, a contingency plan may be enabled to mitigate risk. If configured and allowed,
CAS may notify both the principal and deployer via both email and sms.

### Block Authentication

Prevent the authentication flow to proceed and disallow the establishment of the SSO session.

### Multifactor Authentication

Force the authentication event into a multifactor flow of choice.

## Configuration

Support is enabled by including the following dependency in the overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-electrofence</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

### Remember

- You **MUST** allow and configure CAS to track and record [authentication events](Configuring-Authentication-Events.html).
- You **MUST** allow and configure CAS to [geolocate authentication requests](GeoTracking-Authentication-Requests.html).
- If the selected contingency plan is to force the user into a multifactor authentication flow, you then **MUST** configure CAS for 
[multifactor authentication](Configuring-Multifactor-Authentication.html).
