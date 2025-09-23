package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link BasePac4jOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-oidc")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BasePac4jOidcClientProperties extends Pac4jIdentifiableClientProperties {

    @Serial
    private static final long serialVersionUID = 3359382317533639638L;

    /**
     * The discovery endpoint to locate the provider metadata.
     */
    @RequiredProperty
    private String discoveryUri;

    /**
     * Logout url used for this provider.
     */
    private String logoutUrl;

    /**
     * Whether an initial nonce should be to used
     * initially for replay attack mitigation.
     */
    private boolean useNonce;

    /**
     * Disable PKCE support for the provider.
     */
    private boolean disablePkce;

    /**
     * Requested scope(s).
     */
    @ExpressionLanguageCapable
    private String scope;

    /**
     * The JWS algorithm to use forcefully when validating ID tokens.
     * If none is defined, the first algorithm from metadata will be used.
     */
    private String preferredJwsAlgorithm;

    /**
     * Clock skew in order to account for drift, when validating id tokens.
     */
    @DurationCapable
    private String maxClockSkew = "PT5S";

    /**
     * Custom parameters to send along in authZ requests, etc.
     */
    private Map<String, String> customParams = new HashMap<>();

    /**
     * The response mode specifies how the result of the authorization request is formatted.
     * For backward compatibility the default value is empty, which means the default pac4j (empty) response mode is used.
     * Possible values includes "query", "fragment", "form_post", or "web_message"
     */
    private String responseMode;

    /**
     * The response type tells the authorization server which grant to execute.
     * For backward compatibility the default value is empty, which means the default pac4j ("code") response type is used.
     * Possibles values includes "code", "token" or "id_token".
     */
    private String responseType;

    /**
     * Read timeout of the OIDC client.
     */
    @DurationCapable
    private String connectTimeout = "PT5S";

    /**
     * Connect timeout of the OIDC client.
     */
    @DurationCapable
    private String readTimeout = "PT5S";

    /**
     * Checks if sessions expire with token expiration.
     */
    private boolean expireSessionWithToken;

    /**
     * Default time period advance (in seconds) for considering an access token expired.
     */
    @DurationCapable
    private String tokenExpirationAdvance;

    /**
     * List arbitrary mappings of claims when fetching user profiles.
     * Uses a "directed list" where the allowed
     * syntax would be {@code claim->attribute}.
     */
    private List<String> mappedClaims = new ArrayList<>();

    /**
     * Whether unsigned id tokens issued as plain JWTs are accepted.
     */
    private boolean allowUnsignedIdTokens;

    /**
     * If enabled, try to process the access token as a JWT and include its claims in the profile.
     * Only enable this if there is an agreement between the IdP and CAS about the format of
     * the access token. If not, the token format could change at any time.
     */
    private boolean includeAccessTokenClaims;

    /**
     * The preferred client authentication method
     * that will be chosen for token requests. If none is specified,
     * one will be chosen somewhat randomly based on what the OIDC OP supports.
     */
    private String clientAuthenticationMethod;

    /**
     * Control the list of supported client authentication methods
     * that can be accepted and understood by this integration.
     * Multiple methods may be specified and separated via a comma.
     * Example might be {@code client_secret_basic,client_secret_post,client_secret_jwt}.
     */
    private String supportedClientAuthenticationMethods;

    /**
     * Controls whether the logout token submitted as a JWT should be validated
     * for the correct signature, etc.
     */
    private boolean validateLogoutToken = true;
}
