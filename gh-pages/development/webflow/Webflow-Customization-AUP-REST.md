---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
category: Acceptable Usage Policy
---

{% include variables.html %}

# REST Acceptable Usage Policy

CAS can be configured to use a REST API as the storage mechanism. Upon accepting the policy,
the API is contacted passing along a `username` parameter who has accepted the policy. The expected response status code is `200`.

Furthermore, the API endpoint at `${endpoint}/policy` will be invoked by CAS to fetch the appropriate policy terms.
The API is contacted passing along `username` and `locale` parameters and the expected response status code is `200`. The response
output body is expected to be an instance of `AcceptableUsagePolicyTerms` as such:

```json
{
  "@class": "org.apereo.cas.aup.AcceptableUsagePolicyTerms",
  "code": "screen.aup.policyterms.some.key",
  "defaultText": "Default policy text"
}
```

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-aup-rest" %}

{% include casproperties.html properties="cas.acceptable-usage-policy.rest" %}
