---
layout: default
title: CAS - Password Management - Custom
category: Password Management
---

{% include variables.html %}

# Password Management - Custom

Look up accounts and change passwords in a system CAS does not support by registering your own `PasswordManagementService`.

You may also inject your own implementation for password management into CAS that would itself handle account updates and retrievals.
In order to do this, you will need to design a configuration class that roughly matches the following: 

```java
package org.apereo.cas.pm;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyPasswordConfiguration {

    @Bean
    public PasswordManagementService passwordChangeService() {
        ...
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
