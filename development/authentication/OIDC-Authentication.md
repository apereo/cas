# OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication.html

Allow CAS to act as an [OpenId Connect Provider (OP)](http://openid.net/connect/).

> **Remember**
>
> OpenId Connect is a continuation of the [OAuth protocol](https://apereo.github.io/cas/development/authentication/OAuth-Authentication.html) with some additional variations. If you enable OpenId Connect, you will have automatically enabled OAuth as well. Options and behaviors that are documented for the [OAuth protocol](https://apereo.github.io/cas/development/authentication/OAuth-Authentication.html) support may apply here just the same.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-oidc"
}
```

To learn more about OpenId Connect, please [review this guide](http://openid.net/specs/openid-connect-basic-1_0.html).

The current implementation provides support for:

- [Authorization Code Flow](http://openid.net/specs/openid-connect-basic-1_0.html)
- [Implicit Flow](https://openid.net/specs/openid-connect-implicit-1_0.html)
- [Dynamic Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)
- [WebFinger Issuer Discovery](https://openid.net/specs/openid-connect-discovery-1_0-21.html)
- Administration and registration of [OIDC clients and relying parties](https://apereo.github.io/cas/development/services/Service-Management.html).
- Administration and registration of [OIDC clients and relying parties](https://apereo.github.io/cas/development/services/Service-Management.html) via [Dynamic Client Registration protocol](https://tools.ietf.org/html/draft-ietf-oauth-dyn-reg-management-01).
- Ability to [resolve, map and release claims](https://apereo.github.io/cas/development/integration/Attribute-Release-Policies.html).
- Ability to configure expiration policies for various tokens
- [Federation](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html).

## Endpoints

| Field | Description |
| --- | --- |
| `/oidc/.well-known` | The discovery endpoint used to query for CAS OIDC configuration information and metadata. |
| `/oidc/.well-known/openid-configuration` | Same as `.well-known` discovery endpoint. |
| `/oidc/.well-known/openid-federation` | The specific federation endpoint. |
| `/oidc/.well-known/webfinger` | [WebFinger](https://tools.ietf.org/html/rfc7033) discovery endpoint |
| `/oidc/jwks` | Contains the [server’s public keys](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS.html), which clients may use to verify the digital signatures of access tokens and ID tokens issued by CAS. Accepts an optional `state` query parameter to narrow down keys by their current state (i.e. `current`, `previous`, `future`). |
| `/oidc/authorize` | Authorization requests are handled here. |
| `/oidc/oidcPushAuthorize` | Pushed authorization requests are handled here. |
| `/oidc/profile` | User profile requests are handled here. |
| `/oidc/logout` | Logout requests are handled here. |
| `/oidc/introspect` | Query CAS to detect the status of a given access token via [introspection](https://tools.ietf.org/html/rfc7662). This endpoint expects HTTP basic authentication with OIDC service `client_id` and `client_secret` associated as username and password. |
| `/oidc/accessToken`, `/oidc/token` | Produces authorized access tokens. |
| `/oidc/revoke` | [Revoke](https://tools.ietf.org/html/rfc7009) access or refresh tokens. This endpoint expects HTTP basic authentication with OIDC service `client_id` and `client_secret` associated as username and password. |
| `/oidc/register` | Register clients via the [dynamic client registration](https://tools.ietf.org/html/draft-ietf-oauth-dyn-reg-management-01) protocol. |
| `/oidc/initToken` | Obtain an initial *master* access token required for dynamic client registration when operating in `PROTECTED` mode. |
| `/oidc/clientConfig` | [Update or retrieve client](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Dynamic-Registration.html) application definitions, registered with the server. |
| `/oidc/fetch` | [For a trust anchor or intermediate in a federation](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html), it returns the signed entity statements. |
| `/oidc/list` | [For a trust anchor or intermediate in a federation](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html), it returns the list of the subordinates. |

> **Use Discovery**
>
> The above endpoints are not strictly defined in the OpenID Connect specification. The CAS software may choose to change URL endpoints at any point in time. Do **NOT** hardcode these endpoints in your application configuration. Instead, use the Dynamic Discovery endpoint and parse the discovery document to discover the endpoints.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.core.issuer` (Required; type `String`; default `http://localhost:8080/cas/oidc`): OIDC issuer. All OpenID Connect servers such as CAS are uniquely identified by a URL known as the issuer. This URL serves as the prefix of a service discovery endpoint as specified in the OpenID Connect Discovery standard. This URL must be using the https scheme with no query or fragment component that the identity provider (CAS) asserts as its Issuer Identifier. This also MUST be identical to the `iss` claim value in ID Tokens issued from this issuer, unless overridden in very special circumstances as a last resort. CAS primarily supports a single issuer per deployment/host.
- `cas.authn.oidc.core.accepted-issuers-pattern` (Optional; type `String`; default `a^`): Defines the regular expression pattern that is matched against the calculated issuer from the request. If the issuer that is extracted from the request does not match the `#issuer` defined in the CAS configuration, this pattern acts as a secondary level rule to allow incoming requests to pass through if the match is successful. By default, the pattern is designed to never match anything.
- `cas.authn.oidc.core.authentication-context-reference-mappings` (Optional; type `List<String>`): A mapping of authentication context refs (ACR) values. This is where specific authentication context classes are referenced and mapped to providers that CAS may support mainly for MFA purposes. Example might be `acr-value->mfa-duo`. Support for authentication context class references is implemented in form of `acr_values` as part of the original authorization request, which is mostly taken into account by the multifactor authentication features of CAS. Once successful, `acr` and `amr` values are passed back to the relying party as part of the ID token.
- `cas.authn.oidc.core.claims-map` (Optional; type `Map<String,String>`): Map fixed claims to CAS attributes. Key is the existing claim name for a scope and value is the new attribute that should take its place and value. Claims associated with a scope (i.e. given_name for profile) are fixed in the OpenID specification. In the event that custom arbitrary attributes should be mapped to claims, mappings can be defined in CAS settings to link a CAS-defined attribute to a fixed given scope. For instance, CAS configuration may allow the value of the attribute `sys_given_name` to be mapped and assigned to the claim `given_name` without having an impact on the attribute resolution configuration and all other CAS-enabled applications. If mapping is not defined, by default CAS attributes are expected to match claim names.
- `cas.authn.oidc.core.pairwise-subject-salt` (Optional; type `String`)
- `cas.authn.oidc.core.skew` (Optional; type `String`; default `PT5M`): Skew value used to massage the authentication issue instance.
- `cas.authn.oidc.core.user-defined-scopes` (Optional; type `Map<String,String>`): Mapping of user-defined scopes. Key is the new scope name and value is a comma-separated list of claims mapped to the scope. Such user-defined scopes are also able to override the definition of system scopes. User-defined scopes as well as any and all custom claims that are mapped to the scope must also be defined as scopes and claims supported by CAS in OpenID Connect discovery.

## Sample Client Applications

- [MITREid Sample Java Webapp](https://github.com/apereo/oidc-sample-java-webapp)

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.apereo.cas.oidc" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
