# Simple Multifactor Authentication - Custom Token Management

Source: https://apereo.github.io/cas/development/mfa/Simple-Multifactor-Authentication-TokenManagement-Custom.html

Issue and validate CAS Simple multifactor tokens with your own implementation of `CasSimpleMultifactorAuthenticationService`.

You may define your own multifactor authentication service using the following bean definition and by implementing `CasSimpleMultifactorAuthenticationService`:

```java
@Bean
public CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService() {
    return new MyCasSimpleMultifactorAuthenticationService();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
