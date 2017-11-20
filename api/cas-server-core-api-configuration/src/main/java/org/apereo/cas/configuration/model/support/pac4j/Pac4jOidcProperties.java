package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link Pac4jOidcProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
public class Pac4jOidcProperties extends Pac4jGenericClientProperties {
    private static final long serialVersionUID = 3359382317533639638L;
    /**
     * The type of the provider. "google" and "azure" are also acceptable values.
     */
    private String type = "generic";
    /**
     * The discovery endpoint to locate the provide metadata.
     */
    @RequiredProperty
    private String discoveryUri;

    /**
     * Whether an initial nonce should be to used
     * initially for replay attack mitigation.
     */
    private boolean useNonce;

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
    private Map<String, String> customParams = new HashMap<>();

    public Map<String, String> getCustomParams() {
        return customParams;
    }

    public void setCustomParams(final Map<String, String> customParams) {
        this.customParams = customParams;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getDiscoveryUri() {
        return this.discoveryUri;
    }

    public void setDiscoveryUri(final String discoveryUri) {
        this.discoveryUri = discoveryUri;
    }

    public boolean isUseNonce() {
        return useNonce;
    }

    public void setUseNonce(final boolean useNonce) {
        this.useNonce = useNonce;
    }

    public String getPreferredJwsAlgorithm() {
        return this.preferredJwsAlgorithm;
    }

    public void setPreferredJwsAlgorithm(final String preferredJwsAlgorithm) {
        this.preferredJwsAlgorithm = preferredJwsAlgorithm;
    }

    public int getMaxClockSkew() {
        return this.maxClockSkew;
    }

    public void setMaxClockSkew(final int maxClockSkew) {
        this.maxClockSkew = maxClockSkew;
    }
}

