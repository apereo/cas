# SAML2 Authentication Context Class

Source: https://apereo.github.io/cas/development/installation/Configuring-SAML2-AuthnContextClass.html

Each service may specify a required authentication class, which may overwrite the appropriate field in the ultimate SAML2 response that is sent back to the service provider.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.saml-idp.core.context.authentication-context-class-mappings` (Optional; type `List<String>`): A mapping of authentication context class refs. This is where specific authentication context classes are referenced and mapped to providers that CAS may support mainly for, i.e. MFA purposes. Example might be `urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo`. In delegated authentication scenarios, this can also be a mapping of authentication context class refs, when CAS is proxying/delegating authentication to an external SAML2 identity provider. The requested authentication context as submitted by the service provider is first received by CAS, and then gets mapped to a context class that is passed onto the external identity provider. For example, you might have a scenario where a SAML2 service provider would submit `https://refeds.org/profile/mfa` to CAS, and CAS would translate that to `http://schemas.microsoft.com/claims/multipleauthn` to ultimate route the authentication request to Azure. If no mapping is found, the original context is passed as is. Example might be `https://refeds.org/profile/mfa->http://schemas.microsoft.com/claims/multipleauthn`.
- `cas.authn.saml-idp.core.context.default-authentication-context-class` (Optional; type `String`; default `urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport`): The default authentication context class to include in the response if none is specified via the service.

**Static**

Always use the specified authentication context class in the final response.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "https://spring.io/security/saml-sp",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "requiredAuthenticationContextClass": "https://refeds.org/profile/mfa",
}
```

**External Groovy**

You can always manipulate the authentication context class in more dynamic ways using a Groovy script:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "https://spring.io/security/saml-sp",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "requiredAuthenticationContextClass": "file:///path/to/GroovyScript.groovy"
}
```

The script itself may be designed as:

```groovy
import org.apereo.cas.support.saml.web.idp.profile.builders.*

def run(final Object... args) {
    def (samlContext,logger) = args
    
    logger.info("Building context for entity {}", samlContext.adaptor.entityId)
    /**
      This is where you calculate the final context class...
    */
    return "https://refeds.org/profile/mfa"
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

**Embedded Groovy**

Similar to the external Groovy script option, except the script is embedded inside the service definition:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "https://spring.io/security/saml-sp",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "requiredAuthenticationContextClass": "groovy { return 'https://refeds.org/profile/mfa' } "
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

**Custom**

It is possible to design and inject your authentication context class builder into CAS using the following `@Bean` that would be registered in a `@AutoConfiguration` class:

```java
@Bean
public SamlProfileAuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder() {
    return new MyBuilder();
}
```

Your configuration class needs to be registered with CAS. [See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) for better details.

## Multifactor Authentication

CAS can be instructed via configuration properties to map authentication context classes from SAML2 authentication requests to multifactor authentication profiles. Aside from the configuration adjustments, note that the SAML2 authentication requests **MUST** be properly signed for the multifactor authentication trigger to recognize the relevant profile.
