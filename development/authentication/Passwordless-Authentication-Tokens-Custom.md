# Custom Passwordless Authentication Tokens

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Tokens-Custom.html

Keep passwordless one-time tokens in your own store by implementing `PasswordlessTokenRepository` and registering it with CAS.

You may also define your own token management store using the following bean definition and by implementing `PasswordlessTokenRepository`:

```java
@Bean
public PasswordlessTokenRepository passwordlessTokenRepository() {
    ...
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
