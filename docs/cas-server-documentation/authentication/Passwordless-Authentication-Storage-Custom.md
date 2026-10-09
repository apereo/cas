---
layout: default
title: CAS - Custom Passwordless Authentication Storage
category: Authentication
---
{% include variables.html %}

# Custom Passwordless Authentication Storage

Look up passwordless user accounts in your own store by implementing `PasswordlessUserAccountStore` and registering it with CAS.

You may also define your own user account store using the following 
bean definition and by implementing `PasswordlessUserAccountStore`:

```java 
@Bean
public PasswordlessUserAccountStore passwordlessUserAccountStore() {
    ...
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn 
more about how to register configurations into the CAS runtime.

