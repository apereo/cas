package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * A method by which a client authenticates to the token, revocation, pushed authorization, backchannel authentication
 * and verifiable credential transaction endpoints: an {@link AuthenticationConverter} that reads its credentials from
 * the request, or returns {@code null} when the request carries none, and an {@link AuthenticationProvider} that verifies
 * them. Methods are tried in order, and the first that authenticates the request wins.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OAuth20ClientAuthenticationProvider extends Ordered {

    /**
     * The name of the method.
     *
     * @return the name
     */
    String getName();

    /**
     * Reads the credentials of this method from the request.
     *
     * @return the converter
     */
    AuthenticationConverter getAuthenticationConverter();

    /**
     * Verifies the credentials and returns the authenticated {@link OAuth20ClientAuthenticationToken}, or {@code null}.
     *
     * @return the provider
     */
    AuthenticationProvider getAuthenticationProvider();

    /**
     * Whether the client this method authenticates is taken from elsewhere than the request, so it need not be the client
     * the request names. The pre-authorized code of a verifiable credential offer is one: its client is the one that made
     * the offer, whatever {@code client_id} the wallet sends.
     *
     * @return true or false
     */
    default boolean isRequestedClientIgnored() {
        return false;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
