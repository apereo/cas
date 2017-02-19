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
    private final String username;
    private final String challenge;

    public U2FAuthentication(final String username, final String challenge) {
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
