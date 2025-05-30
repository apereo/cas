package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This is {@link OidcDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcDiscoveryProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 813028615694269276L;

    /**
     * Specifying whether this provider supports use of the claims parameter.
     */
    private boolean claimsParameterSupported = true;

    /**
     * Specifying whether this provider supports use of the {@code request} parameter.
     */
    private boolean requestParameterSupported = true;

    /**
     * Specifying whether this provider supports use of the {@code request_uri} parameter.
     */
    private boolean requestUriParameterSupported = true;

    /**
     * Parameter indicating whether the authorization server provides the {@code iss}
     * parameter in the authorization response.
     */
    private boolean authorizationResponseIssuerParameterSupported;

    /**
     * Boolean value indicating server support for mutual-TLS client certificate-bound access tokens. 
     */
    private boolean tlsClientCertificateBoundAccessTokens;

    /**
     * Boolean parameter indicating
     * whether the authorization server (CAS) accepts authorization request
     * data only via the pushed authorization request method.
     */
    private boolean requirePushedAuthorizationRequests;
    /**
     * Value indicating whether native SSO flows are supported.
     */
    private boolean nativeSsoSupported = true;

    /**
     * List of supported scopes.
     */
    private List<String> scopes = Stream.of("openid", "profile", "email",
        "address", "phone", "offline_access", "device_sso", "client_configuration_scope",
        "uma_authorization", "uma_protection", "client_registration_scope").toList();

    /**
     * List of supported claims.
     */
    private List<String> claims = Stream.of("sub", "acr", "name", "preferred_username", "family_name",
        "given_name", "middle_name", "given_name", "profile", "picture", "nickname", "website",
        "zoneinfo", "locale", "updated_at", "birthdate", "email", "email_verified", "phone_number",
        "phone_number_verified", "address", "gender").toList();

    /**
     * List of supported subject types.
     */
    private List<String> subjectTypes = Stream.of("public", "pairwise").toList();

    /**
     * Supported response types.
     * The Response Mode request parameter response_mode informs the Authorization Server
     * of the mechanism to be used for returning Authorization Response parameters
     * from the Authorization Endpoint. Each Response Type value also defines a
     * default Response Mode mechanism to be used, if no Response Mode is specified using the request parameter.
     */
    private List<String> responseTypesSupported = Stream.of("code", "token", "id_token", "id_token token", "device_code").toList();

    /**
     * Supported response modes.
     */
    private List<String> responseModesSupported = Stream.of("query", "fragment",
        "form_post", "query.jwt", "form_post.jwt", "fragment.jwt").toList();

    /**
     * Supported prompt values.
     * If CAS receives a prompt value that it does not support
     * (not declared in the {@code prompt_values_supported} metadata field) the CAS SHOULD
     * respond with an HTTP 400 (Bad Request) status code and an error value of invalid request.
     */
    private List<String> promptValuesSupported = Stream.of("none", "login", "consent").toList();

    /**
     * Supported authentication methods for introspection.
     */
    private List<String> introspectionSupportedAuthenticationMethods = Stream.of("client_secret_basic").toList();

    /**
     * Supported claim types.
     */
    private List<String> claimTypesSupported = Stream.of("normal").toList();

    /**
     * Supported grant types.
     */
    private List<String> grantTypesSupported = Stream.of(
        "authorization_code",
        "password",
        "client_credentials",
        "refresh_token",
        "urn:openid:params:grant-type:ciba",
        "urn:ietf:params:oauth:grant-type:token-exchange",
        "urn:ietf:params:oauth:grant-type:device_code",
        "urn:ietf:params:oauth:grant-type:uma-ticket").toList();

    /**
     * A array containing a list
     * of the JWS "alg" values supported by the CAS authorization server for
     * DPoP proof JWTs.
     */
    private List<String> dpopSigningAlgValuesSupported = Stream.of("RS256", "RS384", "RS512", "ES256", "ES384", "ES512").toList();

    /**
     * Supported algorithms for id token signing.
     */
    private List<String> idTokenSigningAlgValuesSupported = Stream.of("none", "RS256", "RS384",
        "RS512", "PS256", "PS384",
        "PS512", "ES256", "ES384",
        "ES512", "HS256", "HS384", "HS512").toList();

    /**
     * Supported algorithms for id token encryption.
     */
    private List<String> idTokenEncryptionAlgValuesSupported = Stream.of("RSA1_5", "RSA-OAEP", "RSA-OAEP-256",
        "A128KW", "A192KW", "A256KW", "A128GCMKW", "A192GCMKW", "A256GCMKW",
        "ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW").toList();

    /**
     * Supported encoding strategies for id token encryption.
     */
    private List<String> idTokenEncryptionEncodingValuesSupported = Stream.of("A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512",
        "A128GCM", "A192GCM", "A256GCM").toList();

    /**
     * Accepted values containing a list of the JWS signing algorithms
     * supported by the introspection endpoint to sign the response.
     */
    private List<String> introspectionSignedResponseAlgValuesSupported = Stream.of("none", "RS256", "RS384",
        "RS512", "PS256", "PS384",
        "PS512", "ES256", "ES384",
        "ES512", "HS256", "HS384", "HS512").toList();

    /**
     * Accepted values containing a list of the JWE encryption algorithms ({@code alg} values)
     * supported by the introspection endpoint to encrypt the content encryption key for introspection response.
     */
    private List<String> introspectionEncryptedResponseAlgValuesSupported = Stream.of("RSA1_5", "RSA-OAEP", "RSA-OAEP-256",
        "A128KW", "A192KW", "A256KW", "A128GCMKW", "A192GCMKW", "A256GCMKW",
        "ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW").toList();

    /**
     * Accepted values containing a list of the JWE encryption algorithms ({@code enc} values)
     * supported by the introspection endpoint to encrypt the introspection response.
     */
    private List<String> introspectionEncryptedResponseEncodingValuesSupported = Stream.of("A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512",
        "A128GCM", "A192GCM", "A256GCM").toList();

    /**
     * Supported algorithms for user-info signing.
     */
    private List<String> userInfoSigningAlgValuesSupported = Stream.of("none", "RS256", "RS384",
        "RS512", "PS256", "PS384",
        "PS512", "ES256", "ES384",
        "ES512", "HS256", "HS384", "HS512").toList();

    /**
     * Supported algorithms for user-info encryption.
     */
    private List<String> userInfoEncryptionAlgValuesSupported = Stream.of("RSA1_5", "RSA-OAEP", "RSA-OAEP-256",
        "A128KW", "A192KW", "A256KW", "A128GCMKW", "A192GCMKW", "A256GCMKW",
        "ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW").toList();

    /**
     * Supported encoding strategies for user-info encryption.
     */
    private List<String> userInfoEncryptionEncodingValuesSupported = Stream.of("A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512",
        "A128GCM", "A192GCM", "A256GCM").toList();

    /**
     * List of client authentication methods supported by token endpoint.
     */
    private List<String> tokenEndpointAuthMethodsSupported =
        Stream.of("client_secret_basic", "client_secret_post", "client_secret_jwt", "private_key_jwt", "tls_client_auth").toList();

    /**
     * List of PKCE code challenge methods supported.
     */
    private List<String> codeChallengeMethodsSupported = Stream.of("plain", "S256").toList();

    /**
     * List of ACR values supported.
     * This discovery element contains a list of the supported acr values supported by this server.
     * Support for authentication context class references is implemented in form of {@code acr_values} as part of the original
     * authorization request, which is mostly taken into account by
     * the multifactor authentication features of CAS.
     * Once successful, {@code acr} and {@code amr} values are passed back to the relying party as part of the id token.
     */
    private List<String> acrValuesSupported = new ArrayList<>();

    /**
     * Supported algorithms for request object signing.
     */
    private List<String> requestObjectSigningAlgValuesSupported = Stream.of("none", "RS256", "RS384",
        "RS512", "PS256", "PS384",
        "PS512", "ES256", "ES384",
        "ES512", "HS256", "HS384",
        "HS512").toList();

    /**
     * Supported algorithms for request object encryption.
     */
    private List<String> requestObjectEncryptionAlgValuesSupported = Stream.of("RSA1_5", "RSA-OAEP", "RSA-OAEP-256",
        "A128KW", "A192KW", "A256KW", "A128GCMKW", "A192GCMKW", "A256GCMKW",
        "ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW").toList();

    /**
     * Supported encoding strategies for request object encryption.
     */
    private List<String> requestObjectEncryptionEncodingValuesSupported = Stream.of("A128CBC-HS256",
        "A192CBC-HS384", "A256CBC-HS512",
        "A128GCM", "A192GCM", "A256GCM").toList();

    /**
     * Supported backchannel token delivery modes used for CIBA.
     */
    private List<String> backchannelTokenDeliveryModesSupported = Stream.of("poll", "ping", "push").toList();

    /**
     * List of the JWS signing algorithms (alg values) supported by the CAS for signed authentication requests.
     * If omitted, signed authentication requests are not supported by the CAS.
     */
    private List<String> backchannelAuthenticationRequestSigningAlgValuesSupported = Stream.of("none", "RS256", "RS384",
        "RS512", "PS256", "PS384",
        "PS512", "ES256", "ES384",
        "ES512", "HS256", "HS384",
        "HS512").toList();

    /**
     * Boolean value specifying whether the OP supports the use of the {@code user_code} parameter.
     */
    private boolean backchannelUserCodeParameterSupported;
    
    /**
     * Boolean value indicating support for verified_claims, i.e., the OpenID Connect for Identity Assurance extension.
     */
    private boolean verifiedClaimsSupported = true;

    /**
     * Set containing all supported trust frameworks. This array must have at least one member.
     */
    private Set<String> trustFrameworksSupported;

    /**
     * Set containing all types of identity evidence the OP uses. This array may have zero or more members.
     */
    private Set<String> evidenceSupported;

    /**
     * Needed when {@link #evidenceSupported} contains {@code document} or {@code id_document}.
     * Set containing all identity document types utilized by the CAS for identity verification.
     */
    private Set<String> documentsSupported;

    /**
     * Set containing the validation methods the CAS supports.
     */
    private Set<String> documentsValidationMethodsSupported;

    /**
     * Set containing the verification methods the CAS supports.
     */
    private Set<String> documentsVerificationMethodsSupported;

    /**
     * Needed when evidence_supported contains {@code electronicrecord}. Set containing all
     * electronic record types the CAS supports.
     */
    private Set<String> electronicRecordsSupported;

    /**
     * List of the supported verified claims.
     */
    private Set<String> claimsInVerifiedClaimsSupported;
}
