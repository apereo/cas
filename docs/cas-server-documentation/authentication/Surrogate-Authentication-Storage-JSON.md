---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# JSON Surrogate Authentication Registration

Similar to above, except that surrogate accounts may be defined in an external JSON file whose path is specified via the CAS configuration. The syntax of the JSON file should match the following snippet:

```json
{
    "casuser": ["jsmith", "banderson"],
    "adminuser": ["jsmith", "tomhanks"]
}
```

{% include_cached casproperties.html properties="cas.authn.surrogate.json" %}
