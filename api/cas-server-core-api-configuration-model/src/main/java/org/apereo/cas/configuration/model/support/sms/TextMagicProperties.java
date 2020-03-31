package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link TextMagicProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-sms-textmagic")
@Getter
@Setter
@Accessors(chain = true)
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

    /**
     * Check that whether debugging is enabled for this API client.
     */
    private boolean debugging;

    /**
     * set password for the first HTTP basic authentication.
     */
    private String password;

    /**
     * read timeout (in milliseconds).
     */
    private int readTimeout = 5_000;

    /**
     * connect timeout (in milliseconds).
     */
    private int connectTimeout = 5_000;

    /**
     * Set the User-Agent header's value (by adding to the default header map).
     */
    private String userAgent;

    /**
     * Should SSL connections be verified?
     */
    private boolean verifyingSsl = true;

    /**
     * write timeout (in milliseconds).
     */
    private int writeTimeout;

    /**
     * set API key value for the first API key authentication.
     */
    private String apiKey;

    /**
     * set API key prefix for the first API key authentication.
     */
    private String apiKeyPrefix;
}
