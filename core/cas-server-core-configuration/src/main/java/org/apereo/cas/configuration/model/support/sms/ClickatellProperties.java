package org.apereo.cas.configuration.model.support.sms;

import java.io.Serializable;

/**
 * This is {@link ClickatellProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ClickatellProperties implements Serializable {
    private static final long serialVersionUID = -2147844690349952176L;
    /**
     * Secure token used to establish a handshake with the service.
     */
    private String token;
    /**
     * URL to contact and send messages.
     */
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
