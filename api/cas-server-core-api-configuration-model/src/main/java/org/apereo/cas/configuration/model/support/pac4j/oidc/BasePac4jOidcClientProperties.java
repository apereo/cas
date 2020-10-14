package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.model.support.pac4j.Pac4jIdentifiableClientProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link BasePac4jOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BasePac4jOidcClientProperties extends Pac4jIdentifiableClientProperties {

    private static final long serialVersionUID = 3359382317533639638L;

    /**
     * The discovery endpoint to locate the provide metadata.
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
     * Disable PKCE, even when supported by the IdP.
     */
    private boolean disablePkce;

    /**
     * Requested scope(s).
     */
    private String scope;

    /**
     * The JWS algorithm to use forcefully when validating ID tokens.
     * If none is defined, the first algorithm from metadata will be used.
     */
    private String preferredJwsAlgorithm;

    /**
     * Clock skew in order to account for drift, when validating id tokens.
     */
    private int maxClockSkew;

    /**
     * Custom parameters to send along in authZ requests, etc.
     */
    private Map<String, String> customParams = new HashMap<>(0);

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
    private String connectTimeout = "PT5S";
    /**
     * Connect timeout of the OIDC client.
     */
    private String readTimeout= "PT5S";

    /**
     * Checks if sessions expire with token expiration.
     */
    private boolean expireSessionWithToken;

    /**
     * Default time period advance (in seconds) for considering an access token expired.
     */
    private String tokenExpirationAdvance;

}
