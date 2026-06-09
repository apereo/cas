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
     * @param clientId the client id
     * @param jkt      the jkt
     * @param jwk      the jwk
     * @return the client jwks registration entry
     */
    ClientJwksRegistrationEntry save(String clientId, String jkt, String jwk);

    /**
     * Find by jkt optional.
     *
     * @param clientId the client id
     * @param jkt      the jkt
     * @return the optional
     */
    Optional<ClientJwksRegistrationEntry> findBy(String clientId, String jkt);

    /**
     * Load list.
     *
     * @return the list
     */
    List<ClientJwksRegistrationEntry> load();

    /**
     * Remove by jkt.
     *
     * @param clientId the client id
     * @param jkt      the jkt
     */
    void remove(String clientId, String jkt);

    /**
     * Remove all.
     */
    void removeAll();
}
