# Global - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-Global.html

Global authentication policy that is applied when CAS attempts to vend and validate tickets.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.required-handler-authentication-policy-enabled` (Optional; type `Boolean`; default `false`): Global authentication policy that is applied when CAS attempts to vend and validate tickets. Checks to make sure a particular authentication handler has successfully executed and validated credentials. Required handlers are defined per registered service.

If you wish to design your own global authentication policy, you may define the following bean definition in your environment:

```java
@AutoConfiguration
public class MyConfiguration {
    @Bean
    public AuthenticationPolicy globalAuthenticationPolicy() {
        return new MyAuthenticationPolicy();
    }
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
