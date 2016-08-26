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

    private static class OAuthToken {
        Policy policy = new Policy();

        public Policy getPolicy() {
            return policy;
        }

        public void setPolicy(Policy policy) {
            this.policy = policy;
        }
    }

    public static class Code extends OAuthToken {
        public Code() {
            policy.setNumberOfUses(1);
            policy.setMaxTimeToLiveInSeconds(30);
        }
    }

    public static class AccessToken extends OAuthToken {
        public AccessToken() {
            policy.setTimeToKillInSeconds(7200);
            policy.setMaxTimeToLiveInSeconds(28800);
        }
    }

    public static class RefreshToken extends OAuthToken {
        public RefreshToken() {
            policy.setNumberOfUses(100);
            policy.setTimeToKillInSeconds(604800);
            policy.setMaxTimeToLiveInSeconds(2592000);
        }
    }

    public static class Policy {
        private int numberOfUses = -1;
        private long timeToKillInSeconds = -1;
        private long maxTimeToLiveInSeconds = -1;

        public int getNumberOfUses() {
            return numberOfUses;
        }

        public void setNumberOfUses(int numberOfUses) {
            this.numberOfUses = numberOfUses;
        }

        public long getTimeToKillInSeconds() {
            return timeToKillInSeconds;
        }

        public void setTimeToKillInSeconds(long timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }

        public long getMaxTimeToLiveInSeconds() {
            return maxTimeToLiveInSeconds;
        }

        public void setMaxTimeToLiveInSeconds(long maxTimeToLiveInSeconds) {
            this.maxTimeToLiveInSeconds = maxTimeToLiveInSeconds;
        }
    }
}

