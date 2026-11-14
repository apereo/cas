# Custom Passwordless Authentication Storage

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Storage-Custom.html

Look up passwordless user accounts in your own store by implementing `PasswordlessUserAccountStore` and registering it with CAS.

You may also define your own user account store using the following bean definition and by implementing `PasswordlessUserAccountStore`:

```java
@Bean
public PasswordlessUserAccountStore passwordlessUserAccountStore() {
    ...
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
