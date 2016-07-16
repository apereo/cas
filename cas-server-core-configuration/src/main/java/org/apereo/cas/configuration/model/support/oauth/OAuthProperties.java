package org.apereo.cas.configuration.model.support.oauth;

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
        private long maxTimeToLiveInSeconds = 28800;
        private long timeToKillInSeconds = 7200;

        public long getMaxTimeToLiveInSeconds() {
            return maxTimeToLiveInSeconds;
        }

        public void setMaxTimeToLiveInSeconds(final long maxTimeToLiveInSeconds) {
            this.maxTimeToLiveInSeconds = maxTimeToLiveInSeconds;
        }

        public long getTimeToKillInSeconds() {
            return timeToKillInSeconds;
        }

        public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }
    }

    public static class RefreshToken {
        private long timeToKillInSeconds = 2592000;

        public long getTimeToKillInSeconds() {
            return timeToKillInSeconds;
        }

        public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }
    }
}

