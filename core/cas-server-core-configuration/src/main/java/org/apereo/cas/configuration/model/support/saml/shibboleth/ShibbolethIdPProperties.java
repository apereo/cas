package org.apereo.cas.configuration.model.support.saml.shibboleth;

/**
 * This is {@link ShibbolethIdPProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ShibbolethIdPProperties {
    private String serverUrl;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(final String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
