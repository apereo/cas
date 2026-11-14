# Risk-based Authentication

Source: https://apereo.github.io/cas/development/authentication/Configuring-RiskBased-Authentication.html

Risk-based authentication allows CAS to detect suspicious and seemingly-fraudulent authentication requests based on past user behavior and collected authentication events, statistics, etc. Once and *after* primary authentication where the principal is identified, the authentication transaction is analyzed via a number of configurable criteria and fences to determine how *risky* the attempt may be. The result of the evaluation step is a cumulative risk score that is then weighed against a risk threshold set by the CAS operator. In the event that the authentication attempt is considered risky well beyond the risk threshold, CAS may be allowed to take action and mitigate that risk.

In summary, the story told is:

> If an authentication request is at least [X%] risky, take action to mitigate that risk.

The functionality of this feature is **ENTIRELY** dependent upon collected statistics and authentication events in the past. Without data, there is nothing to analyze and no risk to detect.

Note that evaluation of attempts and mitigation of risks are all recorded in the audit log.

> **Adaptive Authentication**
>
> If you need to preemptively evaluate authentication attempts based on various characteristics of the request, you may be interested in [this guide](https://apereo.github.io/cas/development/mfa/Configuring-Adaptive-Authentication.html) instead.

A few notes to remember:

- You **MUST** allow and configure CAS to track and record [authentication events](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Events.html).
- You **MUST** allow and configure CAS to [geolocate authentication requests](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Requests.html).
- If the selected contingency plan is to force the user into a multifactor authentication flow, you then **MUST** configure CAS for [multifactor authentication](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication.html) and the relevant provider.

## Configuration

Support is enabled by including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-electrofence"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.risk.core.threshold` (Required; type `Double`; default `0.6`): The risk threshold factor beyond which the authentication event may be considered risky.
- `cas.authn.adaptive.risk.core.days-in-recent-history` (Optional; type `Long`; default `30`): Indicates how far back the search in authentication history must go in order to locate authentication events.

## Risk Calculation

You need to configure CAS to allow it to detect suspicious and seemingly-fraudulent authentication requests based on past user behavior and collected authentication events, statistics, etc.

[See this guide](https://apereo.github.io/cas/development/authentication/Configuring-RiskBased-Authentication-Calculation.html) for more info.

## Risk Mitigation

Once an authentication attempt is deemed risky, you need to configure CAS to decide how to handle and respond to the authentication attempt.

[See this guide](https://apereo.github.io/cas/development/authentication/Configuring-RiskBased-Authentication-Mitigation.html) for more info.
