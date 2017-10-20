package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link OAuthAccessTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
public class OAuthAccessTokenProperties implements Serializable {
    private static final long serialVersionUID = -6832081675586528350L;
    /**
     * Hard timeout to kill the access token and expire it.
     */
    private String maxTimeToLiveInSeconds = "PT28800S";
    /**
     * Sliding window for the access token expiration policy.
     * Essentially, this is an idle time out.
     */
    private String timeToKillInSeconds = "PT7200S";
    /**
     * Whether CAS authentication/protocol attributes
     * should be released as part of this access token's validation.
     */
    private boolean releaseProtocolAttributes = true;

    public boolean isReleaseProtocolAttributes() {
        return releaseProtocolAttributes;
    }

    public void setReleaseProtocolAttributes(final boolean releaseProtocolAttributes) {
        this.releaseProtocolAttributes = releaseProtocolAttributes;
    }

    public long getMaxTimeToLiveInSeconds() {
        return Beans.newDuration(maxTimeToLiveInSeconds).getSeconds();
    }

    public void setMaxTimeToLiveInSeconds(final String maxTimeToLiveInSeconds) {
        this.maxTimeToLiveInSeconds = maxTimeToLiveInSeconds;
    }

    public long getTimeToKillInSeconds() {
        return Beans.newDuration(timeToKillInSeconds).getSeconds();
    }

    public void setTimeToKillInSeconds(final String timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }
}
