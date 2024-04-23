---
layout: default
title: CAS - SMS Messaging
category: Notifications
---

{% include variables.html %}

# REST SMS Messaging

Send text messages using a RESTful API. This is typically a `POST` that can be sent to an endpoint via the listed styles below.
A status code of `2xx` is expected from the endpoint.

{% tabs restsms %}

{% tab restsms Query Parameters %}

The following request/query parameters are submitted to the endpoint URL:

| Field             | Description                               |
|-------------------|-------------------------------------------|
| `clientIpAddress` | The client IP address, when available.    |
| `serverIpAddress` | The server IP address, when available.    |
| `from`            | The from address of the text message.     |
| `to`              | The target recipient of the text message. |

The request body contains the actual message. 

{% endtab %}

{% tab restsms Request Body %}
 
In this option, the following parameters are included in the request body as a JSON document:

| Field             | Description                               |
|-------------------|-------------------------------------------|
| `clientIpAddress` | The client IP address, when available.    |
| `serverIpAddress` | The server IP address, when available.    |
| `from`            | The from address of the text message.     |
| `to`              | The target recipient of the text message. |
| `text`            | The target recipient of the text message. |

{% endtab %}

{% endtabs %}

## Configuration

{% include_cached casproperties.html properties="cas.sms-provider.rest" %}
