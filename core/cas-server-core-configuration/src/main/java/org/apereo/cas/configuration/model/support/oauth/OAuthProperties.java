package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.Beans;

/**
 * This is {@link OAuthProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuthProperties {

    private Code code = new Code();
    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(final RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(final Code code) {
        this.code = code;
    }

    public static class Code {
        private int numberOfUses = 1;
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

    public static class AccessToken {
        private String maxTimeToLiveInSeconds = "PT28800S";
        private String timeToKillInSeconds = "PT7200S";
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

    public static class RefreshToken {
        private String timeToKillInSeconds = "P14D";

        public long getTimeToKillInSeconds() {
            return Beans.newDuration(timeToKillInSeconds).getSeconds();
        }

        public void setTimeToKillInSeconds(final String timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }
    }
}

