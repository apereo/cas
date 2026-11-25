# Account (Self-Service) Registration - Custom Provisioning

Source: https://apereo.github.io/cas/development/registration/Account-Registration-Provisioning-Custom.html

Account registration requests can be submitted to a custom implementation that is responsible for managing and storing the account in the appropriate systems of record.

```java
@Bean
public AccountRegistrationProvisionerConfigurer customProvisioningConfigurer() {
    return () -> {
        return new CustomAccountRegistrationProvisioner();
    };
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
