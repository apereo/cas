---
layout: default
title: CAS - SMS Messaging
category: Notifications
---

{% include variables.html %}

# REST SMS Messaging

Send text messages using a RESTful API. This is a `POST` with the following parameters:
            
| Field             | Description                               |
|-------------------|-------------------------------------------|
| `clientIpAddress` | The client IP address.                    |
| `serverIpAddress` | The server IP address.                    |
| `from`            | The from address of the text message.     |
| `to`              | The target recipient of the text message. |

The request body contains the actual message. A status code of `200` is expected from the endpoint.

{% include_cached casproperties.html properties="cas.sms-provider.rest" %}
