package org.apereo.cas.configuration.model.support.sms;

import java.io.Serializable;

/**
 * This is {@link TextMagicProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TextMagicProperties implements Serializable {

    private static final long serialVersionUID = 5645993472155203013L;
    /**
     * Secure token used to establish a handshake.
     */
    private String token;
    /**
     * Username authorized to use the service as the bind account.
     */
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }
}
