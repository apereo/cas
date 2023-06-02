---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email - Twilio SendGrid
   
You may instruct CAS to use [Twilio SendGrid](https://sendgrid.com/) for sending emails.
Support is enabled by including the the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sendgrid" %}

{% include_cached casproperties.html thirdPartyStartsWith="spring.sendgrid" %}

### Custom

You may also define your own email sender that would be tasked to submit emails, etc using the following
bean definition and by implementing `EmailSender`:

```java
@Bean
public EmailSender emailSender() {
    return new MyEmailSender();   
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
