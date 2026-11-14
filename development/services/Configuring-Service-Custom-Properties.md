# Configure Service Custom Properties

Source: https://apereo.github.io/cas/development/services/Configuring-Service-Custom-Properties.html

CAS has ability to add arbitrary attributes to a registered service. These attributes are considered extra metadata about the service that indicate settings such as contact phone number, email, etc or extra attributes and fields that may be used by extensions for custom functionality on a per-service basis.

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "email" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "person@place.edu", "admin@place.edu" ] ]
    }
  }
}
```

Registered service property values can use the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.

Note that you may also extend the CAS configuration schema to define your own [custom properties](https://apereo.github.io/cas/development/webflow/Webflow-Customization-CustomProperties.html).

## Supported Properties

The following properties are available and recognized by CAS for various modules and features:

| Name | Default Value | Type | Group |
| --- | --- | --- | --- |
| `wsfed.relyingPartyIdentifier` |  | `STRING` | `DELEGATED_AUTHN_WSFED` |
| `jwtAsServiceTicket` | `false` | `BOOLEAN` | `JWT_SERVICE_TICKETS` |
| `jwtAsServiceTicketCipherStrategyType` | `ENCRYPT_AND_SIGN` | `STRING` | `JWT_SERVICE_TICKETS` |
| `jwtAsServiceTicketSigningKey` |  | `STRING` | `JWT_SERVICE_TICKETS` |
| `jwtAsServiceTicketEncryptionKey` |  | `STRING` | `JWT_SERVICE_TICKETS` |
| `jwtAsServiceTicketEncryptionAlg` |  | `STRING` | `JWT_SERVICE_TICKETS` |
| `jwtAsServiceTicketSigningEnabled` | `true` | `BOOLEAN` | `JWT_SERVICE_TICKETS` |
| `jwtAsServiceTicketEncryptionEnabled` | `true` | `BOOLEAN` | `JWT_SERVICE_TICKETS` |
| `oidcDynamicClientRegistration` | `false` | `BOOLEAN` | `OIDC` |
| `oidcDynamicClientRegistrationDate` |  | `STRING` | `OIDC` |
| `oidcClientIdMetadataDocument` | `false` | `BOOLEAN` | `OIDC` |
| `oidcClientIdMetadataDocumentRefresh` |  | `STRING` | `OIDC` |
| `oidcResponseModeAsJwtCipherStrategyType` |  | `STRING` | `OIDC` |
| `oidcResponseModeAsJwtCipherSigningEnabled` | `true` | `BOOLEAN` | `JWT_ACCESS_TOKENS` |
| `oidcResponseModeAsJwtCipherEncryptionEnabled` | `true` | `BOOLEAN` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtSigningKey` |  | `STRING` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtCipherStrategyType` | `ENCRYPT_AND_SIGN` | `STRING` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtSigningEnabled` | `true` | `BOOLEAN` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtEncryptionEnabled` | `false` | `BOOLEAN` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtEncryptionKey` |  | `STRING` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtEncryptionAlg` |  | `STRING` | `JWT_ACCESS_TOKENS` |
| `jwtSigningSecret` |  | `STRING` | `JWT_AUTHENTICATION` |
| `jwtSigningSecretAlg` | `HS256` | `STRING` | `JWT_AUTHENTICATION` |
| `jwtEncryptionSecret` |  | `STRING` | `JWT_AUTHENTICATION` |
| `jwtEncryptionSecretAlg` |  | `STRING` | `JWT_AUTHENTICATION` |
| `jwtEncryptionSecretMethod` | `A192CBC-HS384` | `STRING` | `JWT_AUTHENTICATION` |
| `jwtSecretsAreBase64Encoded` | `false` | `BOOLEAN` | `JWT_AUTHENTICATION` |
| `wildcardedServiceDefinition` | `false` | `BOOLEAN` | `REGISTERED_SERVICES` |
| `internalServiceDefinition` | `false` | `BOOLEAN` | `REGISTERED_SERVICES` |
| `skipRequiredServiceCheck` | `false` | `BOOLEAN` | `REGISTERED_SERVICES` |
| `httpHeaderEnableCacheControl` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `httpHeaderEnableXContentOptions` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `httpHeaderEnableStrictTransportSecurity` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `httpHeaderStrictTransportSecurity` |  | `STRING` | `HTTP_HEADERS` |
| `httpHeaderEnableXFrameOptions` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `httpHeaderXFrameOptions` | `DENY` | `STRING` | `HTTP_HEADERS` |
| `httpHeaderEnableContentSecurityPolicy` | `true` | `STRING` | `HTTP_HEADERS` |
| `httpHeaderEnableXSSProtection` | `true` | `BOOLEAN` | `HTTP_HEADERS` |
| `corsAllowCredentials` | `false` | `BOOLEAN` | `CORS` |
| `corsMaxAge` |  | `INTEGER` | `CORS` |
| `corsAllowedOrigins` |  | `STRING` | `CORS` |
| `corsAllowedOriginPatterns` |  | `STRING` | `CORS` |
| `corsAllowedMethods` |  | `STRING` | `CORS` |
| `corsAllowedHeaders` |  | `STRING` | `CORS` |
| `corsExposedHeaders` |  | `STRING` | `CORS` |
| `forceAuthn` |  | `BOOLEAN` | `DELEGATED_AUTHN` |
| `passiveAuthn` |  | `BOOLEAN` | `DELEGATED_AUTHN` |
| `AuthnRequestBindingType` |  | `STRING` | `DELEGATED_AUTHN_SAML2` |
| `AssertionConsumerServiceIndex` |  | `LONG` | `DELEGATED_AUTHN_SAML2` |
| `AttributeConsumingServiceIndex` |  | `LONG` | `DELEGATED_AUTHN_SAML2` |
| `ComparisonType` |  | `STRING` | `DELEGATED_AUTHN_SAML2` |
| `NameIdPolicyFormat` |  | `STRING` | `DELEGATED_AUTHN_SAML2` |
| `NameIdPolicyAllowCreate` |  | `BOOLEAN` | `DELEGATED_AUTHN_SAML2` |
| `ProviderName` |  | `STRING` | `DELEGATED_AUTHN_SAML2` |
| `IssuerFormat` |  | `STRING` | `DELEGATED_AUTHN_SAML2` |
| `UseNameQualifier` |  | `BOOLEAN` | `DELEGATED_AUTHN_SAML2` |
| `AuthnContextClassRefs` |  | `SET` | `DELEGATED_AUTHN_SAML2` |
| `NameIdAttribute` |  | `STRING` | `DELEGATED_AUTHN_SAML2` |
| `WantsAssertionsSigned` |  | `BOOLEAN` | `DELEGATED_AUTHN_SAML2` |
| `WantsResponsesSigned` |  | `BOOLEAN` | `DELEGATED_AUTHN_SAML2` |
| `MaximumAuthenticationLifetime` |  | `LONG` | `DELEGATED_AUTHN_SAML2` |
| `max_age` |  | `INTEGER` | `DELEGATED_AUTHN_OIDC` |
| `scope` |  | `STRING` | `DELEGATED_AUTHN_OIDC` |
| `response_type` |  | `STRING` | `DELEGATED_AUTHN_OIDC` |
| `response_mode` |  | `STRING` | `DELEGATED_AUTHN_OIDC` |
| `captchaIPAddressPattern` | `true` | `SET` | `RECAPTCHA` |
| `captchaEnabled` | `true` | `BOOLEAN` | `RECAPTCHA` |
| `scimOAuthToken` |  | `STRING` | `SCIM` |
| `scimUsername` |  | `STRING` | `SCIM` |
| `scimPassword` |  | `STRING` | `SCIM` |
| `scimTarget` |  | `STRING` | `SCIM` |
