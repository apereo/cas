package org.apereo.cas.sqrl;

/**
 * This is {@link SqrlAuthorization}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlAuthorization {
    private String ipAddress;
    private String identityKey;
    private String correlatedNut;
    private Boolean authorized;

    public SqrlAuthorization(final String ipAddress, final String identityKey,
                  final String correlatedNut, final Boolean authorized) {
        this.ipAddress = ipAddress;
        this.identityKey = identityKey;
        this.correlatedNut = correlatedNut;
        this.authorized = authorized;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIdentityKey() {
        return identityKey;
    }

    public void setIdentityKey(final String identityKey) {
        this.identityKey = identityKey;
    }

    public String getCorrelatedNut() {
        return correlatedNut;
    }

    public void setCorrelatedNut(final String correlatedNut) {
        this.correlatedNut = correlatedNut;
    }

    public Boolean getAuthorized() {
        return authorized;
    }

    public void setAuthorized(final Boolean authorized) {
        this.authorized = authorized;
    }
}
