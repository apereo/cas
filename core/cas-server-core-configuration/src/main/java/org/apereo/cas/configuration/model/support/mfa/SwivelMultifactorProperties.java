package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

/**
 * This is {@link SwivelMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-swivel")
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

    public String getSwivelTuringImageUrl() {
        return swivelTuringImageUrl;
    }

    public void setSwivelTuringImageUrl(final String swivelTuringImageUrl) {
        this.swivelTuringImageUrl = swivelTuringImageUrl;
    }

    public String getSwivelUrl() {
        return swivelUrl;
    }

    public void setSwivelUrl(final String swivelUrl) {
        this.swivelUrl = swivelUrl;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(final String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public boolean isIgnoreSslErrors() {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(final boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    }
}
