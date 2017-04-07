package org.apereo.cas.configuration.model.support.sms;

/**
 * This is {@link ClickatellProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ClickatellProperties {
    private String token;
    private String serverUrl = "https://platform.clickatell.com/messages";

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }
}
