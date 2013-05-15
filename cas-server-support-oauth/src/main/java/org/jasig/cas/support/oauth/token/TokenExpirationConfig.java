package org.jasig.cas.support.oauth.token;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * A convenience bean to inject the CAS TGT timeout value into a bean where
 * other beans can access it.
 * @author Joe McCall
 *
 */
public class TokenExpirationConfig {

    @NotNull
    @Min(0)
    private long accessTokenValiditySeconds;

    /**
     * @return the accessTokenValiditySeconds
     */
    public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    /**
     * @param accessTokenValiditySeconds the accessTokenValiditySeconds to set
     */
    public void setAccessTokenValiditySeconds(final long accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

}
