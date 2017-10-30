package org.apereo.cas.adaptors.u2f;

import java.io.Serializable;

/**
 * This is {@link U2FRegistration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FRegistration implements Serializable {
    private static final long serialVersionUID = 8478965906212939618L;
    private final String challenge;
    private final String appId;

    public U2FRegistration(final String challenge, final String appId) {
        this.challenge = challenge;
        this.appId = appId;
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
