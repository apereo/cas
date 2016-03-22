package org.jasig.cas.services;

import org.jasig.cas.authentication.AuthenticationException;
import org.springframework.core.Ordered;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProvider}
 * that describes an external authentication entity/provider
 * matched against a registered service. Providers may be given
 * the ability to check authentication provider for availability
 * before actually producing a relevant identifier.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationProvider extends Serializable, Ordered {

    /**
     * Provide string.
     *
     * @param service the service
     * @return true /false flag once verification is successful.
     * @throws AuthenticationException the authentication exception
     */
    boolean verify(RegisteredService service) throws AuthenticationException;

    /**
     * Gets id for this provider.
     *
     * @return the id
     */
    String getId();
}
