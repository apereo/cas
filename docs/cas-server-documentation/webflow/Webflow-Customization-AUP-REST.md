---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
category: Acceptable Usage Policy
---

{% include variables.html %}

# REST Acceptable Usage Policy

CAS can be configured to use a REST API as the storage mechanism. Upon accepting the policy,
the API is passed `username` parameter via `POST` who has accepted the policy along with the active `locale`. A `service` parameter
is also passed as the indicator of the target application, if one is available. The expected response status code is `200`.

The condition to determine whether the policy is accepted by the user is controlled typically via a configurable attribute.
If this attribute does not exist or CAS decides that there is not enough evidence to determine an accepted status from this attribute, 
the API endpoint at `${REST_ENDPOINT}/status` is contacted via a `GET` request to determine the policy acceptance status. As before,
this API is passed `username`, `locale` and `service` (if available) parameters and the expected response status code, noting an accepted status, is `200`.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-aup-rest" %}

{% include_cached casproperties.html properties="cas.acceptable-usage-policy.rest" %}

## Policy Terms

The API endpoint at `${REST_ENDPOINT}/policy` will be invoked by CAS via a `GET` to fetch the appropriate policy terms.
The API is passed `username`, `locale` and `service` parameters and the expected response status code is `200`. The response
output body is expected to be an instance of `AcceptableUsagePolicyTerms` as such:

```json
{
  "@class": "org.apereo.cas.aup.AcceptableUsagePolicyTerms",
  "code": "screen.aup.policyterms.some.key",
  "defaultText": "Default policy text"
}
```
   
The provided `code` may point to a language key in the CAS message bundles. The text attached to this key in the relevant bundle
is ultimately pulled out and processed by the CAS UI.
