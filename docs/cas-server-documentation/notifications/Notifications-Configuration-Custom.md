---
layout: default
title: CAS - Notifications
category: Notifications
---

{% include variables.html %}

# Notifications - Custom

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
