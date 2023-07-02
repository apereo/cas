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
package org.apereo.cas;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyAuditConfiguration {

    @Bean
    public AuditTrailExecutionPlanConfigurer myAuditConfigurer() {
        return plan -> {
            var mgr = new MyAuditTrailManager();
            plan.registerAuditTrailManager(mgr);
        };
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
            
