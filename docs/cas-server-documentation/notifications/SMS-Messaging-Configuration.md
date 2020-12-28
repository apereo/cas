---
layout: default
title: CAS - SMS Messaging
category: Notifications
---

{% include variables.html %}

# SMS Messaging

CAS presents the ability to notify users on select actions via SMS messaging. Example 
actions include notification of risky authentication attempts or password reset 
links/tokens. SMS providers supported by CAS are listed below. Note that 
an active/professional subscription may be required for certain providers.

Default support for SMS notifications is automatically enabled/included by the 
relevant modules using the following module:

{% include casmodule.html group="org.apereo.cas" module="cas-server-core-notifications" %}

You need not explicitly include this module in WAR Overlay configurations, except 
when there is a need to access components and APIs at compile-time. See 
below on how to customize or override the default behavior with specific providers.

## Custom

Send text messages using your own custom implementation.

```java
@Bean
public SmsSender smsSender() {
    ...
}    
```     

## Groovy

Send text messages using an external Groovy script.

```groovy
import java.util.*

def run(Object[] args) {
    def from = args[0]
    def to = args[1]
    def message = args[2]
    def logger = args[3]

    logger.debug("Sending message ${message} to ${to} from ${from}")
    true
}
```

{% include {{ version }}/groovy-sms-configuration.md %}

## REST
  
Send text messages using a RESTful API. This is a `POST` with the following parameters:
            
| Field               | Description
|---------------------|---------------------------------------------------
| `clientIpAddress`   | The client IP address.
| `serverIpAddress`   | The server IP address.
| `from`              | The from address of the text message.
| `to`                | The target recipient of the text message.

The request body contains the actual message. A status code of `200` is expected from the endpoint.

{% include {{ version }}/rest-integration.md configKey="cas.sms-provider.rest" %}

## Twilio

To learn more, [visit this site](https://www.twilio.com/).

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-sms-twilio" %}

{% include {{ version }}/twilio-configuration.md %}

## TextMagic

To learn more, [visit this site](https://www.textmagic.com/).

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-sms-textmagic" %}

{% include {{ version }}/textmagic-configuration.md %}

## Clickatell

To learn more, [visit this site](http://www.clickatell.com/).

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-sms-clickatell" %}

{% include {{ version }}/clickatell-configuration.md %}

## Amazon SNS

To learn more, [visit this site](https://docs.aws.amazon.com/sns).

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-sms-aws-sns" %}

{% include {{ version }}/aws-sns-configuration.md %}

{% include {{ version }}/aws-integration.md configKey="cas.sms-provider.sns" %}

## Nexmo

To learn more, [visit this site](https://dashboard.nexmo.com/).

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-sms-nexmo" %}

{% include {{ version }}/nexmo-configuration.md %}
