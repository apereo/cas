package org.apereo.cas.configuration.model.support.sms;

/**
 * This is {@link TwillioProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TwillioProperties {
    private String accountId;
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
