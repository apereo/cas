package org.apereo.cas.acme;

/**
 * This is {@link AcmeChallengeRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated since 7.3.0
 */
@Deprecated(since = "7.3.0", forRemoval = true)
public interface AcmeChallengeRepository {

    /**
     * Add.
     *
     * @param token     the token
     * @param challenge the challenge
     */
    void add(String token, String challenge);

    /**
     * Get authorization challenge.
     *
     * @param token the token
     * @return the string
     */
    String get(String token);
}
