package org.apereo.cas.configuration.model.support.sms;

import java.io.Serializable;

/**
 * This is {@link TwillioProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TwillioProperties implements Serializable {
    private static final long serialVersionUID = -7043132225482495229L;
    /**
     * Twillio account identifier used for authentication.
     */
    private String accountId;

    /**
     * Twillio secret token used for authentication.
     */
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
