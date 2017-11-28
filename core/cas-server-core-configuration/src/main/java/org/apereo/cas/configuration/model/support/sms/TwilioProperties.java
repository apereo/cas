package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

import java.io.Serializable;

/**
 * This is {@link TwilioProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-sms-twilio")
public class TwilioProperties implements Serializable {
    private static final long serialVersionUID = -7043132225482495229L;
    /**
     * Twilio account identifier used for authentication.
     */
    @RequiredProperty
    private String accountId;

    /**
     * Twilio secret token used for authentication.
     */
    @RequiredProperty
    private String token;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }
}
