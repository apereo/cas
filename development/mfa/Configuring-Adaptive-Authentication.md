# Adaptive Authentication

Source: https://apereo.github.io/cas/development/mfa/Configuring-Adaptive-Authentication.html

Adaptive authentication in CAS allows you to accept or reject authentication requests based on certain characteristics of the client browser and/or device. When configured, you are provided with options to block authentication requests from certain locations submitted by certain browser agents. For instance, you may consider authentication requests submitted from `London, UK` to be considered suspicious, or you may want to block requests that are submitted from Internet Explorer, etc.

Adaptive authentication can also be configured to trigger multifactor based on specific days and times. For example, you may wish to trigger multifactor on select days or if the current hour is after 11pm or before 6am. Each rule block may be assigned to an mfa provider where successful matching of rules allows for the multifactor trigger to execute.

You might also be interested in reviewing [Risk-based Authentication](https://apereo.github.io/cas/development/authentication/Configuring-RiskBased-Authentication.html).

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.adaptive.policy.require-timed-multifactor[0].provider-id` (Required; type `String`): The mfa provider id that should be triggered.
- `cas.authn.adaptive.policy.reject-browsers` (Optional; type `String`): Comma-separated list of strings representing browser user agents to be rejected from participating in authentication transactions.
- `cas.authn.adaptive.policy.reject-countries` (Optional; type `String`): Comma-separated list of strings representing countries to be rejected from participating in authentication transactions.
- `cas.authn.adaptive.policy.reject-ip-addresses` (Optional; type `String`): Comma-separated list of strings representing IP addresses to be rejected from participating in authentication transactions.
- `cas.authn.adaptive.policy.require-multifactor` (Optional; type `Map<String,String>`): A map of (`mfaProviderId -> adaptiveRegexPattern`) that tells CAS when to trigger an MFA authentication transaction. This property binds a valid mfa provider to an adaptive regex pattern representing either IP address, user-agent or geolocation. When either of those collected pieces of adaptive data matches configured regex pattern during authentication event, an MFA authentication transaction is triggered for an MFA provider represented by the map's key.
- `cas.authn.adaptive.policy.require-timed-multifactor` (Optional; type `List<TimeBasedAuthenticationProperties>`): This property binds a valid mfa provider to a collection of rules that deal with triggering mfa based on that provider based on properties of date/time. One may want to force mfa during weekends, after hours, etc and the ruleset provides a modest configuration set where time can also be treated as trigger.
- `cas.authn.adaptive.policy.require-timed-multifactor[0].on-days` (Optional; type `List<String>`): Trigger mfa on the following days of the week.
- `cas.authn.adaptive.policy.require-timed-multifactor[0].on-or-after-hour` (Optional; type `long`; default `20`): Trigger mfa after this hour, specified in 24-hour format.
- `cas.authn.adaptive.policy.require-timed-multifactor[0].on-or-before-hour` (Optional; type `long`; default `7`): Trigger mfa before this hour, specified in 24-hour format.

To enable adaptive authentication, you will need to allow CAS to geo-locate authentication requests. To learn more, please [see this guide](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Requests.html)

## IP Intelligence

CAS provides you with the capability to examine the client IP address and decide whether access should be granted. This may be useful to detect bot, proxy or VPN traffic and protect your deployment from fraud, automated attacks, crawlers, etc.

To learn more, please [see this guide](https://apereo.github.io/cas/development/mfa/Adaptive-Authentication-IP-Intelligence.html).
