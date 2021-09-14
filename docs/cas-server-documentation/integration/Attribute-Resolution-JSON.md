---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# JSON Attribute Resolution
     
The following configuration describes how to fetch and retrieve attributes from JSON attribute repositories.

If you wish to directly and separately retrieve attributes from a static JSON source,
the following settings are then relevant:

{% include_cached casproperties.html properties="cas.authn.attribute-repository.json" %}

The format of the file may be:

```json
{
    "user1": {
        "firstName":["Json1"],
        "lastName":["One"]
    },
    "user2": {
        "firstName":["Json2"],
        "eduPersonAffiliation":["employee", "student"]
    }
}
```

