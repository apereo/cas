package org.apereo.cas.adaptors.u2f;

import java.io.Serializable;

/**
 * This is {@link U2FAuthentication}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FAuthentication implements Serializable {
    private static final long serialVersionUID = 334984331545697641L;
    private final String challenge;
    private final String appId;
    private final String keyHandle;
    
    
    public U2FAuthentication(final String challenge, final String appId, final String keyHandle) {
        this.challenge = challenge;
        this.appId = appId;
        this.keyHandle = keyHandle;
    }

    public String getKeyHandle() {
        return keyHandle;
    }

    public String getVersion() {
        return "U2F_V2";
    }

    public String getAppId() {
        return appId;
    }

    public String getChallenge() {
        return challenge;
    }
}
