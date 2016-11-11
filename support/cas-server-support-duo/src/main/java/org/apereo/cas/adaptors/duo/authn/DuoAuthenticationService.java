package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.Credential;

/**
 * This is {@link DuoAuthenticationService}.
 *
 * @param <T> the type parameter
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface DuoAuthenticationService<T> {

    /**
     * Verify the authentication response from Duo.
     *
     * @param credential signed request token
     * @return authenticated user / verified response.
     * @throws Exception if response verification fails
     */
    T authenticate(Credential credential) throws Exception;

    /**
     * Ping provider.
     *
     * @return true /false.
     */
    boolean ping();

    /**
     * Gets api host.
     *
     * @return the api host
     */
    String getApiHost();

    /**
     * Sign request token.
     *
     * @param uid the uid
     * @return the signed token
     */
    String signRequestToken(String uid);
}
