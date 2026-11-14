# Discovery - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Discovery.html

OpenID connect discovery is the process of determining the location of the provider. Discovery returns a JSON listing of the OpenID/OAuth endpoints, supported scopes and claims, public keys used to sign the tokens, and other details. The clients can use this information to construct a request to the CAS OpenID connect server.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.discovery.acr-values-supported` (Optional; type `List<String>`): List of ACR values supported. This discovery element contains a list of the supported acr values supported by this server. Support for authentication context class references is implemented in form of `acr_values` as part of the original authorization request, which is mostly taken into account by the multifactor authentication features of CAS. Once successful, `acr` and `amr` values are passed back to the relying party as part of the ID token.
- `cas.authn.oidc.discovery.authorization-response-issuer-parameter-supported` (Optional; type `Boolean`; default `false`): Parameter indicating whether the authorization server provides the `iss` parameter in the authorization response.
- `cas.authn.oidc.discovery.backchannel-authentication-request-signing-alg-values-supported` (Optional; type `List<String>`): List of the JWS signing algorithms (alg values) supported by the CAS for signed authentication requests. If omitted, signed authentication requests are not supported by the CAS.
- `cas.authn.oidc.discovery.backchannel-token-delivery-modes-supported` (Optional; type `List<String>`): Supported backchannel token delivery modes used for CIBA.
- `cas.authn.oidc.discovery.backchannel-user-code-parameter-supported` (Optional; type `Boolean`; default `false`): Boolean value specifying whether the OP supports the use of the `user_code` parameter.
- `cas.authn.oidc.discovery.claim-types-supported` (Optional; type `List<String>`): Supported claim types.
- `cas.authn.oidc.discovery.claims` (Optional; type `List<String>`): List of supported claims.
- `cas.authn.oidc.discovery.claims-in-verified-claims-supported` (Optional; type `Set<String>`): List of the supported verified claims.
- `cas.authn.oidc.discovery.claims-parameter-supported` (Optional; type `Boolean`; default `true`): Specifying whether this provider supports use of the claims parameter.
- `cas.authn.oidc.discovery.code-challenge-methods-supported` (Optional; type `List<String>`): List of PKCE code challenge methods supported.
- `cas.authn.oidc.discovery.documents-supported` (Optional; type `Set<String>`): Needed when `#evidenceSupported` contains `document` or `id_document`. Set containing all identity document types utilized by the CAS for identity verification.
- `cas.authn.oidc.discovery.documents-validation-methods-supported` (Optional; type `Set<String>`): Set containing the validation methods the CAS supports.
- `cas.authn.oidc.discovery.documents-verification-methods-supported` (Optional; type `Set<String>`): Set containing the verification methods the CAS supports.
- `cas.authn.oidc.discovery.dpop-signing-alg-values-supported` (Optional; type `List<String>`): A array containing a list of the JWS "alg" values supported by the CAS authorization server for DPoP proof JWTs.
- `cas.authn.oidc.discovery.electronic-records-supported` (Optional; type `Set<String>`): Needed when evidence_supported contains `electronicrecord`. Set containing all electronic record types the CAS supports.
- `cas.authn.oidc.discovery.evidence-supported` (Optional; type `Set<String>`): Set containing all types of identity evidence the OP uses. This array may have zero or more members.
- `cas.authn.oidc.discovery.grant-types-supported` (Optional; type `List<String>`): Supported grant types.
- `cas.authn.oidc.discovery.id-token-encryption-alg-values-supported` (Optional; type `List<String>`): Supported algorithms for ID token encryption.
- `cas.authn.oidc.discovery.id-token-encryption-encoding-values-supported` (Optional; type `List<String>`): Supported encoding strategies for ID token encryption.
- `cas.authn.oidc.discovery.id-token-signing-alg-values-supported` (Optional; type `List<String>`): Supported algorithms for ID token signing.
- `cas.authn.oidc.discovery.introspection-encrypted-response-alg-values-supported` (Optional; type `List<String>`): Accepted values containing a list of the JWE encryption algorithms (`alg` values) supported by the introspection endpoint to encrypt the content encryption key for introspection response.
- `cas.authn.oidc.discovery.introspection-encrypted-response-encoding-values-supported` (Optional; type `List<String>`): Accepted values containing a list of the JWE encryption algorithms (`enc` values) supported by the introspection endpoint to encrypt the introspection response.
- `cas.authn.oidc.discovery.introspection-signed-response-alg-values-supported` (Optional; type `List<String>`): Accepted values containing a list of the JWS signing algorithms supported by the introspection endpoint to sign the response.
- `cas.authn.oidc.discovery.introspection-supported-authentication-methods` (Optional; type `List<String>`): Supported authentication methods for introspection.
- `cas.authn.oidc.discovery.native-sso-supported` (Optional; type `Boolean`; default `true`): Value indicating whether native SSO flows are supported.
- `cas.authn.oidc.discovery.prompt-values-supported` (Optional; type `List<String>`): Supported prompt values. If CAS receives a prompt value that it does not support (not declared in the `prompt_values_supported` metadata field) the CAS SHOULD respond with an HTTP 400 (Bad Request) status code and an error value of invalid request.
- `cas.authn.oidc.discovery.request-object-encryption-alg-values-supported` (Optional; type `List<String>`): Supported algorithms for request object encryption. Request objects are encrypted to the encryption keys of the OpenID Connect keystore, so only key management algorithms that work with those keys apply: RSA algorithms for RSA keys, ECDH-ES algorithms for elliptic curve keys. Symmetric key wrapping with keys derived from client secrets is not supported.
- `cas.authn.oidc.discovery.request-object-encryption-encoding-values-supported` (Optional; type `List<String>`): Supported encoding strategies for request object encryption.
- `cas.authn.oidc.discovery.request-object-signing-alg-values-supported` (Optional; type `List<String>`): Supported algorithms for request object signing. Unsigned request objects, whose algorithm is `none`, are refused, so `none` is never advertised, even when listed here.
- `cas.authn.oidc.discovery.request-parameter-supported` (Optional; type `Boolean`; default `true`): Specifying whether this provider supports use of the `request` parameter.
- `cas.authn.oidc.discovery.request-uri-parameter-supported` (Optional; type `Boolean`; default `true`): Specifying whether this provider supports use of the `request_uri` parameter.
- `cas.authn.oidc.discovery.require-pushed-authorization-requests` (Optional; type `Boolean`; default `false`): Boolean parameter indicating whether the authorization server (CAS) accepts authorization request data only via the pushed authorization request method.
- `cas.authn.oidc.discovery.response-modes-supported` (Optional; type `List<String>`): Supported response modes.
- `cas.authn.oidc.discovery.response-types-supported` (Optional; type `List<String>`): Supported response types. The Response Mode request parameter response_mode informs the Authorization Server of the mechanism to be used for returning Authorization Response parameters from the Authorization Endpoint. Each Response Type value also defines a default Response Mode mechanism to be used, if no Response Mode is specified using the request parameter.
- `cas.authn.oidc.discovery.scopes` (Optional; type `List<String>`): List of supported scopes.
- `cas.authn.oidc.discovery.subject-types` (Optional; type `List<String>`): List of supported subject types.
- `cas.authn.oidc.discovery.tls-client-certificate-bound-access-tokens` (Optional; type `Boolean`; default `false`): Boolean value indicating server support for mutual-TLS client certificate-bound access tokens.
- `cas.authn.oidc.discovery.token-endpoint-auth-methods-supported` (Optional; type `List<String>`): List of client authentication methods supported by token endpoint.
- `cas.authn.oidc.discovery.token-endpoint-auth-signing-alg-values-supported` (Optional; type `List<String>`)
- `cas.authn.oidc.discovery.trust-frameworks-supported` (Optional; type `Set<String>`): Set containing all supported trust frameworks. This array must have at least one member.
- `cas.authn.oidc.discovery.user-info-encryption-alg-values-supported` (Optional; type `List<String>`): Supported algorithms for user-info encryption.
- `cas.authn.oidc.discovery.user-info-encryption-encoding-values-supported` (Optional; type `List<String>`): Supported encoding strategies for user-info encryption.
- `cas.authn.oidc.discovery.user-info-signing-alg-values-supported` (Optional; type `List<String>`): Supported algorithms for user-info signing.
- `cas.authn.oidc.discovery.verified-claims-supported` (Optional; type `Boolean`; default `true`): Boolean value indicating support for verified_claims, i.e., the OpenID Connect for Identity Assurance extension.
