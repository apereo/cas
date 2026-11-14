# SAML2 Delegated Authentication

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2.html

CAS can be configured to route the authentication requests to one or more external SAML2 identity providers. Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-pac4j-saml"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.saml[0].keystore-password` (Required; type `String`): The password to use when generating the SP/CAS keystore.
- `cas.authn.pac4j.saml[0].keystore-path` (Required; type `String`): Location of the keystore to use and generate the SP/CAS keystore.
- `cas.authn.pac4j.saml[0].private-key-password` (Required; type `String`): The password to use when generating the private key for the SP/CAS keystore.
- `cas.authn.pac4j.saml[0].service-provider-entity-id` (Required; type `String`; default `https://apereo.org/cas/samlsp`): The entity id of the SP/CAS that is used in the SP metadata generation process.
- `cas.authn.pac4j.saml` (Optional; type `List<Pac4jSamlClientProperties>`): Settings that deal with having SAML2 IdPs as an external delegated-to authentication provider.
- `cas.authn.pac4j.saml[0].accepted-skew` (Optional; type `String`; default `PT300S`): Maximum skew in seconds between SP and IDP clocks. This skew is added onto the `NotOnOrAfter` field in seconds for the SAML response validation.
- `cas.authn.pac4j.saml[0].all-signature-validation-disabled` (Optional; type `boolean`): Whether the signature validation should be disabled. Never set this property to `true` in production.
- `cas.authn.pac4j.saml[0].assertion-consumer-service-index` (Optional; type `int`; default `-1`): Allows the SAML client to select a specific ACS url from the metadata, if defined. A negative value de-activates the selection process and is the default.
- `cas.authn.pac4j.saml[0].attribute-consuming-service-index` (Optional; type `int`): AttributeConsumingServiceIndex attribute of AuthnRequest element. The given index points out a specific AttributeConsumingService structure, declared into the Service Provider (SP)'s metadata, to be used to specify all the attributes that the Service Provider is asking to be released within the authentication assertion returned by the Identity Provider (IdP). This attribute won't be sent with the request unless a positive value (including 0) is defined.
- `cas.authn.pac4j.saml[0].authn-context-class-ref` (Optional; type `List<String>`): Requested authentication context class in authn requests.
- `cas.authn.pac4j.saml[0].authn-context-comparison-type` (Optional; type `String`; default `exact`): Specifies the comparison rule that should be used to evaluate the specified authentication methods. For example, if exact is specified, the authentication method used must match one of the authentication methods specified by the AuthnContextClassRef elements. AuthContextClassRef element require comparison rule to be used to evaluate the specified authentication methods. If not explicitly specified "exact" rule will be used by default. Other acceptable values are minimum, maximum, better.
- `cas.authn.pac4j.saml[0].auto-redirect-type` (Optional; type `DelegationAutoRedirectTypes`; default `NONE`): Auto-redirect to this client. Available values are as follows: - `SERVER`: Redirect on the server side, typically making CAS invisible. - `CLIENT`: Redirect on the client side using browser redirects, etc. - `NONE`: Do nothing and let the selection take place manually.
- `cas.authn.pac4j.saml[0].blocked-signature-signing-algorithms` (Optional; type `List<String>`): Collection of signing signature blocked algorithms, if any, to override the global defaults.
- `cas.authn.pac4j.saml[0].callback-url` (Optional; type `String`): Callback URL to use to return the flow back to the CAS server one the identity provider is successfully done. This may be used at the discretion of the client and its type to build service parameters, redirect URIs, etc. If none is specified, the CAS server's login endpoint will be used as the basis of the final callback url.
- `cas.authn.pac4j.saml[0].callback-url-type` (Optional; type `Pac4jBaseClientProperties.CallbackUrlTypes`; default `QUERY_PARAMETER`): Determine how the callback url should be resolved. Default is `CallbackUrlTypes#QUERY_PARAMETER`. Available values are as follows: - `PATH_PARAMETER`: Path parameter callback url. The client name is added to the path of the callback URL. - `QUERY_PARAMETER`: Query parameter callback url. The client name is added to the path of a query parameter. - `NONE`: No callback url. No name is added to the callback URL to be able to distinguish the client.
- `cas.authn.pac4j.saml[0].certificate-expiration-days` (Optional; type `int`; default `7300`): Define the validity period for the certificate in number of days. The end-date of the certificate is controlled by this setting, when defined as a value greater than zero.
- `cas.authn.pac4j.saml[0].certificate-name-to-append` (Optional; type `String`): A name to append to signing certificates generated. The named part appended can be useful to identify for which clientName it was generated If no name is provided the default certificate name will be used.
- `cas.authn.pac4j.saml[0].certificate-signature-alg` (Optional; type `String`; default `SHA1WithRSA`): Certificate signature algorithm to use when generating the certificate.
- `cas.authn.pac4j.saml[0].client-name` (Optional; type `String`): Name of the client mostly for UI purposes and uniqueness. This name, with 'non-word' characters converted to '-' (e.g. "This Org (New)" becomes "This-Org--New-") is added to the "class" attribute of the redirect link on the login page, to allow for custom styling of individual IdPs (e.g. for an organization logo).
- `cas.authn.pac4j.saml[0].css-class` (Optional; type `String`): CSS class that should be assigned to this client.
- `cas.authn.pac4j.saml[0].destination-binding` (Optional; type `String`; default `urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect`): The destination binding to use when creating authentication requests.
- `cas.authn.pac4j.saml[0].display-name` (Optional; type `String`): Indicate the title or display name of the client for decoration and client presentation purposes. If left blank, the client original name would be used by default.
- `cas.authn.pac4j.saml[0].enabled` (Optional; type `boolean`; default `true`): Whether the client/external identity provider should be considered active and enabled for integration purposes.
- `cas.authn.pac4j.saml[0].force-auth` (Optional; type `boolean`): Whether authentication requests should be tagged as forced auth.
- `cas.authn.pac4j.saml[0].force-keystore-generation` (Optional; type `boolean`): Force generation of the keystore.
- `cas.authn.pac4j.saml[0].logout-request-binding` (Optional; type `String`; default `urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect`): The destination binding to use when creating logout requests.
- `cas.authn.pac4j.saml[0].logout-response-binding-type` (Optional; type `String`): Control the logout response binding type during logout operations as invoked by an external IdP and in response to logout requests.
- `cas.authn.pac4j.saml[0].mapped-attributes` (Optional; type `List<String>`): Describes the map of attributes that are to be fetched from the credential (map keys) and then transformed/renamed using map values before they are put into a profile. An example might be to fetch `givenName` from credential and rename it to `urn:oid:2.5.4.42` or vice versa. Note that this setting only applies to attribute names, and not friendly-names. List arbitrary mappings of claims. Uses a "directed list" where the allowed syntax would be `givenName->urn:oid:2.5.4.42`.
- `cas.authn.pac4j.saml[0].maximum-authentication-lifetime` (Optional; type `String`; default `PT3600S`): Once you have an authenticated session on the identity provider, usually it won't prompt you again to enter your credentials and it will automatically generate a new assertion for you. By default, the SAML client will accept assertions based on a previous authentication for one hour. You can adjust this behavior by modifying this setting. The unit of time here is seconds. To disable this check entirely, set the value to zero.
- `cas.authn.pac4j.saml[0].message-store-factory` (Optional; type `String`; default `org.pac4j.saml.store.EmptyStoreFactory`): Factory implementing this interface provides services for storing and retrieval of SAML messages for e.g. verification of retrieved responses. The default factory is an always empty store. You may choose `org.pac4j.saml.store.HttpSessionStore` instead which allows SAML messages to be stored in a distributed session store especially required for high availability deployments and validation operations. Available options are: - `EMPTY`: Uses the `EmptyStoreFactory` - `SESSION`: Uses the `HttpSessionStore` - Fully-qualified class name of the message store implementation. Also note that the message store implementation can be supplied and configured at runtime as a Spring `@Bean` with the type `SAMLMessageStoreFactory` which, if found in the available application context, will override all other options.
- `cas.authn.pac4j.saml[0].name-id-attribute` (Optional; type `String`): The attribute name that should be used and extracted from the SAML2 response to identify and build a NameID value, when the response is processed and consumed.
- `cas.authn.pac4j.saml[0].name-id-policy-allow-create` (Optional; type `TriStateBoolean`; default `TRUE`): Flag to indicate whether the allow-create flags for nameid policies should be set to true, false or ignored/defined. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.pac4j.saml[0].name-id-policy-format` (Optional; type `String`): NameID policy to request in the authentication requests.
- `cas.authn.pac4j.saml[0].partial-logout-as-success` (Optional; type `boolean`; default `true`): Logouts are only successful if the IdP was able to inform all services, otherwise it will respond with `PartialLogout`. This setting allows clients such as CAS to ignore such server-side behavior. If the IdP reports back a partial logout, this setting instructs CAS whether it should accept or deny that response.
- `cas.authn.pac4j.saml[0].passive` (Optional; type `boolean`): Whether authentication requests should be tagged as passive.
- `cas.authn.pac4j.saml[0].principal-id-attribute` (Optional; type `String`): The attribute found in the identity provider response that may be used to establish the authenticated user and build a profile for CAS.
- `cas.authn.pac4j.saml[0].propagate-logout` (Optional; type `boolean`; default `true`): Indicate whether the logout should be propagated from the CAS server to the delegated identity server.
- `cas.authn.pac4j.saml[0].provider-name` (Optional; type `String`): Provider name set for the saml authentication request. Sets the human-readable name of the requester for use by the presenter's user agent or the identity provider.
- `cas.authn.pac4j.saml[0].request-initiator-url` (Optional; type `String`): When generating SAML2 metadata, configure and set the request initiator location attribute.
- `cas.authn.pac4j.saml[0].requested-attributes.friendly-name` (Optional; type `String`): Attribute friendly name.
- `cas.authn.pac4j.saml[0].requested-attributes.name` (Optional; type `String`): Attribute name.
- `cas.authn.pac4j.saml[0].requested-attributes.name-format` (Optional; type `String`; default `urn:oasis:names:tc:SAML:2.0:attrname-format:uri`): Attribute name format.
- `cas.authn.pac4j.saml[0].requested-attributes.required` (Optional; type `boolean`): Whether this attribute is required and should be marked so in the metadata.
- `cas.authn.pac4j.saml[0].requested-attributes[0].friendly-name` (Optional; type `String`): Attribute friendly name.
- `cas.authn.pac4j.saml[0].requested-attributes[0].name` (Optional; type `String`): Attribute name.
- `cas.authn.pac4j.saml[0].requested-attributes[0].name-format` (Optional; type `String`; default `urn:oasis:names:tc:SAML:2.0:attrname-format:uri`): Attribute name format.
- `cas.authn.pac4j.saml[0].requested-attributes[0].required` (Optional; type `boolean`): Whether this attribute is required and should be marked so in the metadata.
- `cas.authn.pac4j.saml[0].response-binding-type` (Optional; type `String`): The SAML2 response binding type to use when generating metadata. This ultimately controls the binding type of the assertion consumer service in the metadata. Default value is typically `urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST`.
- `cas.authn.pac4j.saml[0].response-destination-mandatory` (Optional; type `boolean`; default `true`): When validating the response, ensure it has a value set for the `Destination` attribute.
- `cas.authn.pac4j.saml[0].saml2-attribute-converter` (Optional; type `String`): Controls the way SAML2 attributes are converted from the authentication response into pac4j attributes. By default, values of complex types are serialized into a single attribute. To change this behaviour, a converter class implementing the `AttributeConverter` interface.
- `cas.authn.pac4j.saml[0].sign-authn-request` (Optional; type `boolean`): Whether or not the authnRequest should be signed.
- `cas.authn.pac4j.saml[0].sign-service-provider-logout-request` (Optional; type `boolean`): Whether or not the Logout Request sent from the SP should be signed.
- `cas.authn.pac4j.saml[0].sign-service-provider-metadata` (Optional; type `boolean`): Whether or not SAML SP metadata should be signed when generated.
- `cas.authn.pac4j.saml[0].signature-algorithms` (Optional; type `List<String>`): Collection of signing signature algorithms, if any, to override the global defaults.
- `cas.authn.pac4j.saml[0].signature-canonicalization-algorithm` (Optional; type `String`): The signing signature canonicalization algorithm, if any, to override the global defaults.
- `cas.authn.pac4j.saml[0].signature-reference-digest-methods` (Optional; type `List<String>`): Collection of signing signature reference digest methods, if any, to override the global defaults.
- `cas.authn.pac4j.saml[0].single-logout-service-url` (Optional; type `String`): When generating SAML2 metadata, configure and set the single logout service URL attribute.
- `cas.authn.pac4j.saml[0].supported-protocols` (Optional; type `List<String>`; default `urn:oasis:names:tc:SAML:2.0:protocol`): When generating SAML2 metadata, configure and set the list of supported protocols in the metadata.
- `cas.authn.pac4j.saml[0].use-name-qualifier` (Optional; type `boolean`; default `true`): Whether name qualifiers should be produced in the final saml response.
- `cas.authn.pac4j.saml[0].wants-assertions-signed` (Optional; type `boolean`): Whether metadata should be marked to request sign assertions.
- `cas.authn.pac4j.saml[0].wants-responses-signed` (Optional; type `boolean`): Whether a response has to be mandatory signed.

## Metadata Management

Please [see this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata.html).

## Per Service Customizations

Th configuration for the external SAML2 identity provider is typically done at build time via CAS configuration settings and applies to all applications and relying parties. You may override certain aspects this configuration on a per application basis by assigning dedicated [properties to the service definition](https://apereo.github.io/cas/development/services/Configuring-Service-Custom-Properties.html).

The following properties are available and recognized by CAS for various modules and features:

| Name | Default Value | Type | Group |
| --- | --- | --- | --- |
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

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org",
  "name" : "Example",
  "id" : 1,
  "properties" : {
    "@class" : "java.util.HashMap",
    "AuthnContextClassRefs" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "https://refeds.org/profile/mfa" ] ]
    },
    "WantsAssertionsSigned" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "false" ] ]
    }
  }
}
```

See [registered service properties](https://apereo.github.io/cas/development/services/Configuring-Service-Custom-Properties.html) for more details.

## Identity Provider Discovery Service

Please [see this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Discovery.html).

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.pac4j" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>

<Logger name="org.opensaml" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>

<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
