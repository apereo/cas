---
layout: default
title: CAS - Notifications - Custom
category: Notifications
---

{% include variables.html %}

# Notifications - Custom

Deliver notifications through a channel CAS does not support by implementing `NotificationSender` and registering it with CAS.

You may define your own custom notification sender using the following
bean definition and by implementing `NotificationSender`:

```java
@Bean
public NotificationSenderExecutionPlanConfigurer myNotificationSender(
    return new NotificationSenderExecutionPlanConfigurer() {
        @Override
        public NotificationSender configureNotificationSender() {
            return new MyNotificationSender();
        }
    };
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
