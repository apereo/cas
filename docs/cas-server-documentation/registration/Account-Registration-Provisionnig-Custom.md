---
layout: default
title: CAS - Account Registration Provisioning
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration - Custom Provisioning

Account registration requests can be submitted to a custom implementation that 
is responsible for managing and storing the account in the appropriate systems of record.

```java
@Bean
public AccountRegistrationProvisionerConfigurer customProvisioningConfigurer() {
    return () -> {
        return new CustomAccountRegistrationProvisioner();
    };
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
