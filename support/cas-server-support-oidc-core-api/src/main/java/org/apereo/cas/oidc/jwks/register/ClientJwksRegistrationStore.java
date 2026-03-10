package org.apereo.cas.oidc.jwks.register;

import module java.base;

/**
 * This is {@link ClientJwksRegistrationStore}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface ClientJwksRegistrationStore {
    /**
     * Save.
     *
     * @param jkt the jkt
     * @param jwk the jwk
     * @return the client jwks registration entry
     */
    ClientJwksRegistrationEntry save(String jkt, String jwk);

    /**
     * Find by jkt optional.
     *
     * @param jkt the jkt
     * @return the optional
     */
    Optional<ClientJwksRegistrationEntry> findByJkt(String jkt);

    /**
     * Load list.
     *
     * @return the list
     */
    List<ClientJwksRegistrationEntry> load();

    /**
     * Remove by jkt.
     *
     * @param jkt the jkt
     */
    void removeByJkt(String jkt);

    /**
     * Remove all.
     */
    void removeAll();
}
