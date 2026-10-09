---
layout: default
title: CAS - JSON Surrogate Authentication Registration
category: Authentication
---
{% include variables.html %}


# JSON Surrogate Authentication Registration

Decide who may impersonate whom with a JSON file that maps each user to the accounts they may log in as.

Surrogate accounts may be defined in an external JSON file whose path is specified via the CAS configuration. The syntax of the JSON file should match the 
following snippet:

```json
{
    "casuser": ["jsmith", "banderson"],
    "adminuser": ["jsmith", "tomhanks"]
}
```

{% include_cached casproperties.html properties="cas.authn.surrogate.json" %}
