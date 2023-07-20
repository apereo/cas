---
layout: default
title: CAS - Adaptive Risk-based Authentication
category: Authentication
---
{% include variables.html %}

# Risk-based Authentication

Risk-based authentication allows CAS to detect suspicious and 
seemingly-fraudulent authentication requests based on past user behavior
and collected authentication events, statistics, etc. Once and *after* 
primary authentication where the principal is identified,
the authentication transaction is analyzed via a number of configurable 
criteria and fences to determine how *risky* the attempt may be.
The result of the evaluation step is a cumulative risk score that is then 
weighed against a risk threshold set by the CAS operator.
In the event that the authentication attempt is considered risky well 
beyond the risk threshold, CAS may be allowed to take action and
mitigate that risk.

In summary, the story told is:

>If an authentication request is at least [X%] risky, take action to mitigate that risk.

The functionality of this feature is **ENTIRELY** dependent upon collected statistics and authentication events in the past.
Without data, there is nothing to analyze and no risk to detect.

Note that evaluation of attempts and mitigation of risks are all recorded in the audit log.

<div class="alert alert-info">:information_source: <strong>Adaptive Authentication</strong><p>
If you need to preemptively evaluate authentication attempts based on various characteristics of the request,
you may be interested in <a href="../mfa/Configuring-Adaptive-Authentication.html">this guide</a> instead.</p></div>

A few notes to remember:

- You **MUST** allow and configure CAS to track and record [authentication events](Configuring-Authentication-Events.html).
- You **MUST** allow and configure CAS to [geolocate authentication requests](GeoTracking-Authentication-Requests.html).
- If the selected contingency plan is to force the user into a multifactor authentication flow, you then **MUST** configure CAS for
  [multifactor authentication](../mfa/Configuring-Multifactor-Authentication.html) and the relevant provider.

## Configuration

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-electrofence" %}

{% include_cached casproperties.html properties="cas.authn.adaptive.risk.core" %}

## Risk Calculation

You need to configure CAS to allow it to detect suspicious and
seemingly-fraudulent authentication requests based on past user behavior
and collected authentication events, statistics, etc. 

[See this guide](Configuring-RiskBased-Authentication-Calculation.html) for more info.

## Risk Mitigation

Once an authentication attempt is deemed risky, you need to configure CAS to decide how to
handle and respond to the authentication attempt.

[See this guide](Configuring-RiskBased-Authentication-Mitigation.html) for more info.
