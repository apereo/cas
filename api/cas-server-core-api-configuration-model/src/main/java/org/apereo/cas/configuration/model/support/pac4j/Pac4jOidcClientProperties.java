package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Pac4jOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")

@Getter
@Setter
public class Pac4jOidcClientProperties extends Pac4jIdentifiableClientProperties {

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
     * Logout url used for this provider.
     */
    private String logoutUrl;

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

    /**
     * Tenant Id as required by Microsoft Azure integrations.
     */
    private String azureTenantId;
}
