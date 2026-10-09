---
layout: default
title: CAS - Sending Email - Custom
category: Notifications
---

{% include variables.html %}

# Sending Email - Custom

Send email through a service CAS does not support by implementing `EmailSender` and registering it with CAS.

You may define your own email sender that would be tasked to submit emails, etc using the following
bean definition and by implementing `EmailSender`:

```java
@Bean
public EmailSender emailSender() {
    return new MyEmailSender();   
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
