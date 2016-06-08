package org.apereo.cas.configuration.model.support.mfa;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for cas.mfa.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.mfa", ignoreUnknownFields = false)
public class MfaProperties {

    private String authenticationContextAttribute = "authnContextClass";
    private String globalFailureMode = "CLOSED";

    private Yubikey yubikey = new Yubikey();

    
    public String getGlobalFailureMode() {
        return globalFailureMode;
    }

    public void setGlobalFailureMode(final String globalFailureMode) {
        this.globalFailureMode = globalFailureMode;
    }

    public String getAuthenticationContextAttribute() {
        return authenticationContextAttribute;
    }

    public void setAuthenticationContextAttribute(final String authenticationContextAttribute) {
        this.authenticationContextAttribute = authenticationContextAttribute;
    }


    public Yubikey getYubikey() {
        return yubikey;
    }

    public void setYubikey(final Yubikey yubikey) {
        this.yubikey = yubikey;
    }

    public static class Yubikey {
        private Integer clientId;
        private String secretKey = "";
        private int rank;

        public Integer getClientId() {
            return clientId;
        }

        public void setClientId(final Integer clientId) {
            this.clientId = clientId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(final String secretKey) {
            this.secretKey = secretKey;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(final int rank) {
            this.rank = rank;
        }
    }
}
