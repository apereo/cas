package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
@JsonFilter("OidcDiscoveryProperties")
public class OidcDiscoveryProperties implements Serializable {

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
     * List of supported scopes.
     */
    private List<String> scopes = Stream.of("openid", "profile", "email", "address", "phone", "offline_access")
        .collect(Collectors.toList());

    /**
     * List of supported claims.
     */
    private List<String> claims = Stream.of("sub", "name", "preferred_username", "family_name",
        "given_name", "middle_name", "given_name", "profile", "picture", "nickname", "website",
        "zoneinfo", "locale", "updated_at", "birthdate", "email", "email_verified", "phone_number",
        "phone_number_verified", "address", "gender").collect(Collectors.toList());

    /**
     * List of supported subject types.
     */
    private List<String> subjectTypes = Stream.of("public", "pairwise").collect(Collectors.toList());

    /**
     * Supported response types.
     */
    private List<String> responseTypesSupported = Stream.of("code", "token", "id_token token").collect(Collectors.toList());

    /**
     * Supported authentication methods for introspection.
     */
    private List<String> introspectionSupportedAuthenticationMethods = Stream.of("client_secret_basic").collect(Collectors.toList());

    /**
     * Supported claim types.
     */
    private List<String> claimTypesSupported = Stream.of("normal").collect(Collectors.toList());

    /**
     * Supported grant types.
     */
    private List<String> grantTypesSupported = Stream.of("authorization_code", "password", "client_credentials", "refresh_token").collect(Collectors.toList());

    /**
     * Supported algorithms for id token signing.
     */
    private List<String> idTokenSigningAlgValuesSupported = Stream.of("none", "RS256", "RS384",
            "RS512", "PS256", "PS384",
            "PS512", "ES256", "ES384",
            "ES512", "HS256", "HS384", "HS512")
        .collect(Collectors.toList());

    /**
     * Supported algorithms for id token encryption.
     */
    private List<String> idTokenEncryptionAlgValuesSupported = Stream.of("RSA1_5", "RSA-OAEP", "RSA-OAEP-256",
            "A128KW", "A192KW", "A256KW", "A128GCMKW", "A192GCMKW", "A256GCMKW",
            "ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW")
        .collect(Collectors.toList());

    /**
     * Supported encoding strategies for id token encryption.
     */
    private List<String> idTokenEncryptionEncodingValuesSupported = Stream.of("A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512",
            "A128GCM", "A192GCM", "A256GCM")
        .collect(Collectors.toList());

    /**
     * Supported algorithms for user-info signing.
     */
    private List<String> userInfoSigningAlgValuesSupported = Stream.of("none", "RS256", "RS384",
            "RS512", "PS256", "PS384",
            "PS512", "ES256", "ES384",
            "ES512", "HS256", "HS384", "HS512")
        .collect(Collectors.toList());

    /**
     * Supported algorithms for user-info encryption.
     */
    private List<String> userInfoEncryptionAlgValuesSupported = Stream.of("RSA1_5", "RSA-OAEP", "RSA-OAEP-256",
            "A128KW", "A192KW", "A256KW", "A128GCMKW", "A192GCMKW", "A256GCMKW",
            "ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW")
        .collect(Collectors.toList());

    /**
     * Supported encoding strategies for user-info encryption.
     */
    private List<String> userInfoEncryptionEncodingValuesSupported = Stream.of("A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512",
            "A128GCM", "A192GCM", "A256GCM")
        .collect(Collectors.toList());

    /**
     * List of client authentication methods supported by token endpoint.
     */
    private List<String> tokenEndpointAuthMethodsSupported =
        Stream.of("client_secret_basic", "client_secret_post", "client_secret_jwt", "private_key_jwt").collect(Collectors.toList());

    /**
     * List of PKCE code challenge methods supported.
     */
    private List<String> codeChallengeMethodsSupported = Stream.of("plain", "S256").collect(Collectors.toList());

    /**
     * List of ACR values supported.
     * This discovery element contains a list of the supported acr values supported by this server.
     */
    private List<String> acrValuesSupported = new ArrayList<>();

    /**
     * Supported algorithms for request object signing.
     */
    private List<String> requestObjectSigningAlgValuesSupported = Stream.of("none", "RS256", "RS384",
            "RS512", "PS256", "PS384",
            "PS512", "ES256", "ES384",
            "ES512", "HS256", "HS384",
            "HS512")
        .collect(Collectors.toList());

    /**
     * Supported algorithms for request object encryption.
     */
    private List<String> requestObjectEncryptionAlgValuesSupported = Stream.of("RSA1_5", "RSA-OAEP", "RSA-OAEP-256",
            "A128KW", "A192KW", "A256KW", "A128GCMKW", "A192GCMKW", "A256GCMKW",
            "ECDH-ES", "ECDH-ES+A128KW", "ECDH-ES+A192KW", "ECDH-ES+A256KW")
        .collect(Collectors.toList());

    /**
     * Supported encoding strategies for request object encryption.
     */
    private List<String> requestObjectEncryptionEncodingValuesSupported = Stream.of("A128CBC-HS256",
            "A192CBC-HS384", "A256CBC-HS512",
            "A128GCM", "A192GCM", "A256GCM")
        .collect(Collectors.toList());
}
