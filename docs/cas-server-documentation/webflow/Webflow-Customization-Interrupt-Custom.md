---
layout: default
title: CAS - Custom Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Custom Authentication Interrupt

Decide when to interrupt the login flow with your own `InterruptInquirer`, registered with CAS as a Spring bean.

If you wish to design your own interrupt strategy to make 
inquiries, you can design your component to make determinations:

```java
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyInterruptConfiguration {
    @Bean
    public InterruptInquirer myInterruptInquirer() {
      ...
    }

    @Bean
    public InterruptInquiryExecutionPlanConfigurer myInterruptInquiryExecutionPlanConfigurer(
        @Qualifier("myInterruptInquirer")
        InterruptInquirer myInterruptInquirer) {
        return plan -> {
            plan.registerInterruptInquirer(myInterruptInquirer);
        };
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn 
more about how to register configurations into the CAS runtime.

