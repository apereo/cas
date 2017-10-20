package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link OAuthCodeProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
public class OAuthCodeProperties implements Serializable {
    private static final long serialVersionUID = -7687928082301669359L;
    /**
     * Number of times this code is valid and can be used.
     */
    private int numberOfUses = 1;
    /**
     * Duration in seconds where the code is valid.
     */
    private long timeToKillInSeconds = 30;

    public int getNumberOfUses() {
        return numberOfUses;
    }

    public void setNumberOfUses(final int numberOfUses) {
        this.numberOfUses = numberOfUses;
    }

    public long getTimeToKillInSeconds() {
        return timeToKillInSeconds;
    }

    public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }
}
