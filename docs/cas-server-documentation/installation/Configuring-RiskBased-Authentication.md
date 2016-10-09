---
layout: default
title: CAS - Adaptive Risk-based Authentication
---

# Risk-based Authentication

Risk-based authentication allows CAS to detect suspicious and seemingly-fraudulent authentication requests based on past user behavior
and collected authentication events, statistics, etc. Once after primary authentication where the principal is identified,
the authentication transaction is analyzed via a number of configurable criteria and fences to determine how *risky* the attempt may be.
The result of the evaluation step is a comulative risk score that is then weighed against a risk threshold set by the CAS operator.
In the event that the authentication attempt is considered risky well beyond the risk threshold, CAS may be allowed to take action and
mitigate that risk. 

Simply put, the story told is:

> If an authentication request is at least [60%] risky, take action to mitigate that risk. 

The functionality of this feature is **ENTIRELY** dependant upon having collected statistics and authentication events in the past.
Without data, there is nothing to analyze and no risk to detect.

Note that evaluation of attempts and mitigation of risks are all recorded in the CAS audit log.

## Risk Calculation

One or more risk calculators may be enabled to allow an analysis of each authentication request:

### IP Address

### Browser User Agent

### Geolocation

### Date/Time

## Risk Mitigation

Once an authentication attempt is deemed risky, a contingency plan may be enabled to mitigate risk. 

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


