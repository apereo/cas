---
layout: default 
title: CAS - Phone Calls 
category: Notifications
---

{% include variables.html %}

# Phone Calls - Custom Operators

You may define your own phone operator that would be tasked to make calls, etc using the following
bean definition and by implementing `PhoneCallOperator`:

```java
@Bean
public PhoneCallOperator phoneCallOperator() {
    return new MyPhoneCallOperator();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
