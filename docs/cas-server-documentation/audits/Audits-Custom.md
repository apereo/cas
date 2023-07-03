---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Custom Audits

If you wish to create your own auditor implementation, you will need to
design an `AuditTrailManager` component and register it with CAS:

```java
@Bean
public AuditTrailExecutionPlanConfigurer myAuditConfigurer() {
    return plan -> {
        var mgr = new MyAuditTrailManager();
        plan.registerAuditTrailManager(mgr);
        
        /*
            Optionally, define your own action/resource resolvers 
            for Spring beans with execution points that are tagged with @Audit annotation.
            
            plan.registerAuditActionResolver("MyAction", new MyAuditActionResolver());
            plan.registerAuditResourceResolver("MyResource", new MyAuditResourceResolver());
        */
    };
}
```

Audit records are typically tagged and recorded with client and server IP addresses. If you need to override the default
behavior and extract the IP addresses based on custom logic, you will need to
design an `ClientInfoResolver` component and register it with CAS:

```java
@Bean
public ClientInfoResolver casAuditClientInfoResolver() {
    return new MyClientInfoResolver();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
            
