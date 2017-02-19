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
    private final String username;
    private final String challenge;

    public U2FRegistration(final String username, final String challenge) {
        this.username = username;
        this.challenge = challenge;
    }

    public String getUsername() {
        return username;
    }

    public String getChallenge() {
        return challenge;
    }
}
