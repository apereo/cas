---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# JSON Authentication Interrupt

{% include_cached casproperties.html properties="cas.interrupt.json" %}

This strategy reaches out to a static JSON resource that contains a map of 
usernames linked to various interrupt policies. This option is most 
useful during development, testing and demos.

```json
{
  "casuser" : {
    "message" : "Announcement message <strong>goes here</strong>.",
    "links" : {
      "Go to Location1" : "https://www.location1.com",
      "Go to Location2" : "https://www.location2.com"
    },
    "block" : false,
    "ssoEnabled" : false,
    "interrupt" : true,
    "autoRedirect" : false,
    "autoRedirectAfterSeconds" : -1,
    "data" : {
      "field1" : [ "value1", "value2" ],
      "field2" : [ "value3", "value4" ]
    }
  }
}
```
