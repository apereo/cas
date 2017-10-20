package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

import java.io.Serializable;

/**
 * This is {@link TextMagicProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-sms-textmagic")
public class TextMagicProperties implements Serializable {

    private static final long serialVersionUID = 5645993472155203013L;
    /**
     * Secure token used to establish a handshake.
     */
    @RequiredProperty
    private String token;
    /**
     * Username authorized to use the service as the bind account.
     */
    @RequiredProperty
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
