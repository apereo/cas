---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Custom Authentication Interrupt

If you wish to design your own interrupt strategy to make 
inquiries, you can design your component to make determinations:

```java
package org.apereo.cas.support.interrupt;

@Configuration(value = "myInterruptConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyInterruptConfiguration {
    @Bean
    public InterruptInquirer interruptInquirer() {
      ...
    }

    @Bean
    public InterruptInquiryExecutionPlanConfigurer myInterruptInquiryExecutionPlanConfigurer() {
        return plan -> {
            plan.registerInterruptInquirer(interruptInquirer());
        };
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn 
more about how to register configurations into the CAS runtime.

