package org.apereo.cas.configuration.model.support.pac4j;

/**
 * This is {@link Pac4jCasProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class Pac4jCasProperties {
    /**
     * The CAS server login url.
     */
    private String loginUrl;
    /**
     * CAS protocol to use.
     * Acceptable values are <code>CAS10, CAS20, CAS20_PROXY, CAS30, CAS30_PROXY, SAML</code>.
     */
    private String protocol;

    public String getLoginUrl() {
        return this.loginUrl;
    }

    public void setLoginUrl(final String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }
}
