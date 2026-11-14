# Claims - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Claims.html

OpenID connect claims are treated as normal CAS attributes that need to be [resolved, mapped and released](https://apereo.github.io/cas/development/integration/Attribute-Release-Policies.html).

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.core.issuer` (Required; type `String`; default `http://localhost:8080/cas/oidc`): OIDC issuer. All OpenID Connect servers such as CAS are uniquely identified by a URL known as the issuer. This URL serves as the prefix of a service discovery endpoint as specified in the OpenID Connect Discovery standard. This URL must be using the https scheme with no query or fragment component that the identity provider (CAS) asserts as its Issuer Identifier. This also MUST be identical to the `iss` claim value in ID Tokens issued from this issuer, unless overridden in very special circumstances as a last resort. CAS primarily supports a single issuer per deployment/host.
- `cas.authn.oidc.core.accepted-issuers-pattern` (Optional; type `String`; default `a^`): Defines the regular expression pattern that is matched against the calculated issuer from the request. If the issuer that is extracted from the request does not match the `#issuer` defined in the CAS configuration, this pattern acts as a secondary level rule to allow incoming requests to pass through if the match is successful. By default, the pattern is designed to never match anything.
- `cas.authn.oidc.core.authentication-context-reference-mappings` (Optional; type `List<String>`): A mapping of authentication context refs (ACR) values. This is where specific authentication context classes are referenced and mapped to providers that CAS may support mainly for MFA purposes. Example might be `acr-value->mfa-duo`. Support for authentication context class references is implemented in form of `acr_values` as part of the original authorization request, which is mostly taken into account by the multifactor authentication features of CAS. Once successful, `acr` and `amr` values are passed back to the relying party as part of the ID token.
- `cas.authn.oidc.core.claims-map` (Optional; type `Map<String,String>`): Map fixed claims to CAS attributes. Key is the existing claim name for a scope and value is the new attribute that should take its place and value. Claims associated with a scope (i.e. given_name for profile) are fixed in the OpenID specification. In the event that custom arbitrary attributes should be mapped to claims, mappings can be defined in CAS settings to link a CAS-defined attribute to a fixed given scope. For instance, CAS configuration may allow the value of the attribute `sys_given_name` to be mapped and assigned to the claim `given_name` without having an impact on the attribute resolution configuration and all other CAS-enabled applications. If mapping is not defined, by default CAS attributes are expected to match claim names.
- `cas.authn.oidc.core.pairwise-subject-salt` (Optional; type `String`)
- `cas.authn.oidc.core.skew` (Optional; type `String`; default `PT5M`): Skew value used to massage the authentication issue instance.
- `cas.authn.oidc.core.user-defined-scopes` (Optional; type `Map<String,String>`): Mapping of user-defined scopes. Key is the new scope name and value is a comma-separated list of claims mapped to the scope. Such user-defined scopes are also able to override the definition of system scopes. User-defined scopes as well as any and all custom claims that are mapped to the scope must also be defined as scopes and claims supported by CAS in OpenID Connect discovery.

## Scope-based Claims

Please see [this guide](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Claims-ScopeBased.html).

## Mapping Claims

Please see [this guide](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Claims-Mapping.html).

## Releasing Claims

Please see [this guide](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Claims-Release.html).

## Pairwise Identifiers

Please see [this guide](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Claims-Pairwise.html).

## Subject Identifier Claim

Please see [this guide](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Claims-Sub.html).
