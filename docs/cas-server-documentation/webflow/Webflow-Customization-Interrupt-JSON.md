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
    "messageCode" : "interrupt.message.body",
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

## Interrupt Payload

Each interrupt strategy is ultimately tasked to produce a response that contains the following settings:

| Field                      | Description                                                                                                                                                           |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `message`                  | Announcement message to display on the screen.                                                                                                                        |
| `messageCode`              | The language bundle key that points to an actual message to be displayed on the screen. Should be used as an alternative to the `message` field.                      |
| `links`                    | A map of links to display on the screen where key is the link text and value is the destination.                                                                      |
| `interrupt`                | `true/false` to indicate whether CAS should interrupt the authentication flow.                                                                                        |
| `block`                    | `true/false` to indicate whether CAS should block the authentication flow altogether.                                                                                 |
| `ssoEnabled`               | `true/false` to indicate whether CAS should permit the authentication but not establish SSO.                                                                          |
| `autoRedirect`             | `true/false` to indicate whether CAS should auto-redirect to the first provided link.                                                                                 |
| `autoRedirectAfterSeconds` | Indicate whether CAS should auto-redirect after the configured number of seconds. The default is `-1`, meaning delayed redirect functionality should not be executed. |
| `data`                     | A map of key-value pairs to pass along to the UI.                                                                                                                     |

