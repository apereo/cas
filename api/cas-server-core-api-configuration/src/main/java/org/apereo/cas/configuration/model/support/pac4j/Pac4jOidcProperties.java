package org.apereo.cas.configuration.model.support.pac4j;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Pac4jOidcProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Slf4j
@Getter
@Setter
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
}
