package org.apereo.cas.configuration.model.support.sms;

/**
 * This is {@link TextMagicProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TextMagicProperties {
    private String token;
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
