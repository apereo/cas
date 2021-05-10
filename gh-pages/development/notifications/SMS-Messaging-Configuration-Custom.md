---
layout: default
title: CAS - SMS Messaging
category: Notifications
---

{% include variables.html %}

# Custom SMS Messaging

Send text messages using your own custom implementation.

```java
@Bean
public SmsSender smsSender() {
    ...
}    
```     

To learn more about how to extend and customize the CAS configuration, please [review this guide](../configuration/Configuration-Management-Extensions.html).
