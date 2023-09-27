---
layout: default
title: CAS - Simple Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Simple Multifactor Authentication - Custom Token Management

You may define your own multifactor authentication service using the following
bean definition and by implementing `CasSimpleMultifactorAuthenticationService`:

```java
@Bean
public CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService() {
    return new MyCasSimpleMultifactorAuthenticationService();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
