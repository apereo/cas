---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Chaining

Multiple access strategies can be combined together to form complex rules and conditions in a chain. Using chains,
one can implement advanced Boolean logic to group results together. Note that chains can contain other chains as well.

The following access strategy chain allows service access if the authenticated principal,

- has an attribute `key1` with a value of `value1` **AND** an attribute `key2` with a value of `value2`.

...**OR**...

- has an attribute `key3` with a value of `value3`.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class": "org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy",
    "strategies": [ "java.util.ArrayList",
      [ {
        "@class": "org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy",
        "strategies": [ "java.util.ArrayList",
          [
            {
              "@class": "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
              "requiredAttributes": {
                "@class": "java.util.LinkedHashMap",
                "key1": [ "java.util.LinkedHashSet", [ "value1" ] ]
              }
            },
            {
              "@class": "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
              "requiredAttributes": {
                "@class": "java.util.LinkedHashMap",
                "key2": [ "java.util.LinkedHashSet", [ "value2" ] ]
              }
            }
          ]
        ],
        "operator": "AND"
      },
        {
          "@class": "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
          "requiredAttributes": {
            "@class": "java.util.LinkedHashMap",
            "key3": [ "java.util.LinkedHashSet", [ "value3" ] ]
          }
        }
      ]
    ],
    "operator": "OR"
  }
}
```
