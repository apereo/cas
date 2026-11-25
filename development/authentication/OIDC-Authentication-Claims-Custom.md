# Custom Claims - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Claims-Custom.html

If you wish to design your own claim assembly strategy and collect claims into an ID token, you may define the following bean definition in your environment:

```java
@AutoConfiguration
public class MyOidcConfiguration {
    @Bean
    public OidcIdTokenClaimCollector oidcIdTokenClaimCollector() {
        return new MyIdTokenClaimCollector();
    }
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

## Groovy Claims

Custom claims can also be collected and assembled using a Groovy script.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.id-token.collector-script.location` (Optional; type `Resource`)
- `cas.authn.oidc.id-token.generate-for-jwt-bearer-grant-type` (Optional; type `Boolean`; default `false`): Flag to indicate whether the JWT bearer grant type is supported for the purposes of requesting ID tokens. Setting this to true will force to generate ID tokens when the JWT bearer grant type is used.
- `cas.authn.oidc.id-token.include-id-token-claims` (Optional; type `Boolean`; default `true`): As per OpenID Connect Core section 5.4, "The Claims requested by the `profile`, `email`, `address`, and `phone` scope values are returned from the userinfo endpoint", except for `response_type`=`id_token`, where they are returned in the id_token (as there is no access token issued that could be used to access the userinfo endpoint). The Claims requested by the profile, email, address, and phone scope values are returned from the userinfo endpoint when a `response_type` value is used that results in an access token being issued. However, when no access token is issued (which is the case for the `response_type` value `id_token`), the resulting Claims are returned in the ID Token. Setting this flag to true will force CAS to include claims in the ID token regardless of the response type. Note that this setting **MUST ONLY** be used as a last resort, to stay compliant with the specification as much as possible. ** DO NOT** use this setting without due consideration. Note that this setting is set to `true` by default mainly provided to preserve backward compatibility with previous CAS versions that included claims into the ID token without considering the response type. The behavior of this setting may change and it may be removed in future CAS releases.
- `cas.authn.oidc.id-token.max-time-to-live-in-seconds` (Optional; type `String`; default `PT8H`): Hard timeout to kill the ID token and expire it.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The script itself may be designed as such:

```groovy
def collect(Object... args) {
    def (claims, name, values, registeredService, applicationContext, logger) = args
    println("Collecting individual claim ${name} for ${registeredService?.getName()}")
    claims.setStringClaim("cn", "CAS User")
}

def conclude(Object... args) {
    def (claims, registeredService, applicationContext, logger) = args
    println("Finalizing all claims for ${registeredService?.getName()}")
    claims.setStringClaim("givenName", "ApereoCAS")
    claims.setStringListClaim("roles", ["admin", "user"])
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `claims` | The `JwtClaims` object representing the ID token claims. |
| `name` | The name of the claim being collected, when passed. |
| `values` | The list of values associated with the claim, when passed. |
| `registeredService` | The object representing the registered service. |
| `applicationContext` | The object representing the Spring `ApplicationContext`. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
