---
layout: default
title: CAS - Releasing Principal Id
category: Attributes
---

{% include variables.html %}

# Default Principal Id

The default configuration which need not explicitly be defined, returns the resolved
principal id as the username for this service.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider",
    "canonicalizationMode" : "NONE",
    "scope": "example.org"
  }
}
```

If you do not need to adjust the behavior of this provider (i.e. to modify the `canonicalization` mode),
then you can leave out this block entirely. Furthermore, if you do not need the final value to be scoped to the defined value,
you may also leave out the `scope` value.
