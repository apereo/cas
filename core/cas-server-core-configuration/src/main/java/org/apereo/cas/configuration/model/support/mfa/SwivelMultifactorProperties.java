package org.apereo.cas.configuration.model.support.mfa;

/**
 * This is {@link SwivelMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SwivelMultifactorProperties extends BaseMultifactorProvider {
    private static final long serialVersionUID = -7409451053833491119L;

    private String swivelTuringImageUrl;
    private String swivelUrl;
    private String sharedSecret;
    private boolean ignoreSslErrors;

    public SwivelMultifactorProperties() {
        setId("mfa-swivel");
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
