package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.Beans;

import java.io.Serializable;

/**
 * This is {@link OAuthProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuthProperties implements Serializable {
    private static final long serialVersionUID = 2677128037234123907L;
    /**
     * Settings related to oauth grants.
     */
    private Grants grants = new Grants();
    /**
     * Settings related to oauth codes.
     */
    private Code code = new Code();
    /**
     * Settings related to oauth access tokens.
     */
    private AccessToken accessToken = new AccessToken();
    /**
     * Settings related to oauth refresh tokens.
     */
    private RefreshToken refreshToken = new RefreshToken();

    public Grants getGrants() {
        return grants;
    }

    public void setGrants(final Grants grants) {
        this.grants = grants;
    }

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

    public static class AccessToken {
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

    public static class RefreshToken {
        /**
         * Hard timeout beyond which the refresh token is considered expired.
         */
        private String timeToKillInSeconds = "P14D";

        public long getTimeToKillInSeconds() {
            return Beans.newDuration(timeToKillInSeconds).getSeconds();
        }

        public void setTimeToKillInSeconds(final String timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }
    }
    
    public static class Grants {
        /**
         * Resource owner grant settings.
         */
        private ResourceOwner resourceOwner = new ResourceOwner();

        public ResourceOwner getResourceOwner() {
            return resourceOwner;
        }

        public void setResourceOwner(final ResourceOwner resourceOwner) {
            this.resourceOwner = resourceOwner;
        }

        public static class ResourceOwner {
            /**
             * Whether using the resource-owner grant should
             * enforce authorization rules and per-service policies
             * based on a service parameter is provided as a header
             * outside the normal semantics of the grant and protocol.
             */
            private boolean requireServiceHeader;

            public boolean isRequireServiceHeader() {
                return requireServiceHeader;
            }

            public void setRequireServiceHeader(final boolean requireServiceHeader) {
                this.requireServiceHeader = requireServiceHeader;
            }
        }
    }
}

