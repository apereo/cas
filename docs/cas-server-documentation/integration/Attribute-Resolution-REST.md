---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# REST Attribute Resolution
     
The following configuration describes how to fetch and retrieve attributes from REST attribute repositories.

{% include_cached casproperties.html properties="cas.authn.attribute-repository.rest" %}

The authenticating user id is passed in form of a request parameter under `username`. The response is expected
to be a JSON map as such:

```json
{
  "name" : "JohnSmith",
  "age" : 29,
  "messages": ["msg 1", "msg 2", "msg 3"]
}
```


