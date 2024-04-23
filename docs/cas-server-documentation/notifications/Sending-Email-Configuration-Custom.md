---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email - Custom

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
