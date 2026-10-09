package org.apereo.cas.oidc.authn;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * Hands out and checks the server-provided challenges that clients put in the {@code challenge} claim of their client
 * attestation proofs of possession (OAuth 2.0 Attestation-Based Client Authentication, section 6).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OidcClientAttestationChallengeService {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcClientAttestationChallengeService";

    /**
     * Whether challenges are handed out, and therefore required in proofs of possession.
     *
     * @return true when enabled
     */
    boolean isEnabled();

    /**
     * Create a challenge.
     *
     * @return the challenge
     * @throws Throwable the throwable
     */
    String create() throws Throwable;

    /**
     * Whether a challenge was handed out by CAS and has not expired. A challenge may be used more than once while valid.
     *
     * @param challenge the challenge
     * @return true when valid
     */
    boolean isValid(@Nullable String challenge);
}
