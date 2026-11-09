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
        return (authentication, registeredService, service, credential) -> {
            ...
            return InterruptResponse.none();
        };
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

An interrupt inquirer is given the authentication, the matching registered service and service (if any) and the credential,
and does not receive the Spring Webflow request context. Inquirers are also asked outside the login flow,
such as by the actuator endpoints that report the interrupt payload for a user, so your inquirer should not depend on
the login flow; if it needs the current HTTP request, it can obtain it from the request attributes of the current thread,
if any, via `HttpRequestUtils.getHttpServletRequestFromRequestAttributes()`.

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn 
more about how to register configurations into the CAS runtime.

