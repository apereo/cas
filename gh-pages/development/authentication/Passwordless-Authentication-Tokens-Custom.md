---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Custom Passwordless Authentication Tokens

You may also define your own token management store using the following 
bean definition and by implementing `PasswordlessTokenRepository`:

```java 
@Bean
public PasswordlessTokenRepository passwordlessTokenRepository() {
    ...
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn 
more about how to register configurations into the CAS runtime.
