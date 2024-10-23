---
layout: default
title: CAS - Configuring Service Matching Strategy
category: Services
---

{% include variables.html %}

# Configuring Service Matching Strategy

Authentication requests that carry a client application identifier are compared against the service identifier
that is assigned to a service definition. By default, the service identifier is treated as a regular expression pattern
that needs to be properly encoded and defined for matching operation to successfully execute. This strategy can be
defined on a per-service basis to allow for alternative options or a full strategy implementation that may want to
take external factors and variables into account.

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
Note that you cannot whitelist an <i>IP address</i> of an application. You can only whitelist the application URL. 
The identifier of the application is almost always its URL; not its source or origin. 
In scenarios where an application might present the same IP address and multiple URLs, each URL would then
needs to registered with CAS, or you may come up with a unifying pattern that captures all URL forms.
</p></div>

{% tabs matchingstrategy %}

{% tab matchingstrategy Full Regex <i class="fa fa-registered px-1"></i> %}

This is the default option that treats the `serviceId` as a regular expression. With this option,
CAS tries to match the expression against the entire requested service identifier and implicitly
adds a `^` at the start and `$` at the end of the defined pattern, meaning it will not look for substring matches.

A sample JSON file follows:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "https://.*",
  "name": "sample",
  "id": 1,
  "matchingStrategy": {
    "@class": "org.apereo.cas.services.FullRegexRegisteredServiceMatchingStrategy"
  }
}
```

{% endtab %}

{% tab matchingstrategy Partial Regex <i class="fa fa-registered px-1"></i> %}

This strategy treats the `serviceId` as a regular expression. With this option, CAS will look and allow for substring matches.

A sample JSON file follows:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "\\d\\d\\d",
  "name": "sample",
  "id": 1,
  "matchingStrategy": {
    "@class": "org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy"
  }
}
```

For example, the above pattern will match against `https://example123.com`.

{% endtab %}


{% tab matchingstrategy Literal %}

This strategy treats the `serviceId` as a literal text and will look for exact matches. This might be useful in scenarios where you
may not wish to deal with encoding individual/special characters such as `?` in the URL.

A sample JSON file follows:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "https://example.com?key=value",
  "name": "sample",
  "id": 1,
  "matchingStrategy": {
    "@class": "org.apereo.cas.services.LiteralRegisteredServiceMatchingStrategy",
    "caseInsensitive": true
  }
}
```

{% endtab %}

{% endtabs %}
