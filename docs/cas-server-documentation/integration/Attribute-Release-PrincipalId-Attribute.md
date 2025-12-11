---
layout: default
title: CAS - Releasing Principal Id
category: Attributes
---

{% include variables.html %}

# Attribute-based Principal Id

Returns an attribute that is already resolved for the principal as the username for this service. If the attribute
is not available, the default principal id will be used.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 1,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "cn",
    "canonicalizationMode" : "UPPER",
    "scope": "example.org",
    "removePattern": ""
  }
}
```

The following settings and properties are available:

| Property               | Description                                                                                                  |
|------------------------|--------------------------------------------------------------------------------------------------------------|
| `canonicalizationMode` | Optional. Transform the username to uppercase, or lowercase. Allowed values are `UPPER`, `LOWER` or `NONE`.  |
| `scope`                | Optional. Allows you to **scope** the value to a given domain, by appending the domain to the final user id. |
| `removePattern`        | Optional. A regular expression pattern that would remove all matches from the final user id.                 |

<div class="alert alert-info">:information_source: <strong>Note</strong><p>
You may define multiple attributes in a comma-separated list for the `usernameAttribute` property.
The first attribute that resolves a non-empty value will be used.
</p></div>

The following examples should provide useful:

{% tabs accessstrategyprincipal %}

{% tab accessstrategyprincipal Attribute <i class="fa fa-id-card px-1"></i> %}
        
Select the username from the resolved attribute, `cn`, and make sure it's transformed into an uppercase string.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 1,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "cn",
    "canonicalizationMode" : "UPPER"
  }
}
```

{% endtab %}

{% tab accessstrategyprincipal Advanced %}

Select the username from the resolved attribute, `email`, and make sure it's transformed into an uppercase string.
Then, remove all values that match the pattern `@.+` from the result, and scope the result to `example.org`.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 1,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "email",
    "canonicalizationMode" : "UPPER",
    "scope": "example.org",
    "removePattern": "@.+"
  }
}
```

If the `email` attribute has the value of `casuser@apereo.org`, the final username resolved would be: `CASUSER@EXAMPLE.ORG`

{% endtab %}

{% endtabs %}
