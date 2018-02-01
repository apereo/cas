package org.apereo.cas.configuration.model.support.mfa;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link SwivelMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-swivel")
@Slf4j
@Getter
@Setter
public class SwivelMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-swivel";

    private static final long serialVersionUID = -7409451053833491119L;

    /**
     * URL endpoint response to generate a turing image.
     */
    @RequiredProperty
    private String swivelTuringImageUrl;

    /**
     * Swivel endpoint url for verification of credentials.
     */
    @RequiredProperty
    private String swivelUrl;

    /**
     * Shared secret to authenticate against the swivel server.
     */
    @RequiredProperty
    private String sharedSecret;

    /**
     * Control whether SSL errors should be ignored by the swivel server.
     */
    private boolean ignoreSslErrors;

    public SwivelMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
