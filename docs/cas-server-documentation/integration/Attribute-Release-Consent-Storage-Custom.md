---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# Custom - Attribute Consent Storage

You may also inject your own implementation for attribute consent management
into CAS that would itself handle storing consent decisions, etc. In order
to do this, you will need to design a configuration class that roughly matches the following:

```java
package org.apereo.cas.consent;

@Configuration(value = "MyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration {

    @Bean
    public ConsentRepository consentRepository() {
        ...
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to
learn more about how to register configurations into the CAS runtime.
