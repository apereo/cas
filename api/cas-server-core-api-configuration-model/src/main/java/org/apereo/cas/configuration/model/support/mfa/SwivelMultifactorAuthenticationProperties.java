package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SwivelMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-swivel")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SwivelMultifactorProperties")
public class SwivelMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {

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

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    public SwivelMultifactorAuthenticationProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
