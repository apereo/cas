package org.jasig.cas.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DefaultAuthenticationContext} represents a concrete
 * implementation of the authentication context. It acts as a carrier
 * to hold authentication sessions established during the processing
 * of a given request, and identifies which of those sessions
 * can be considered the primary.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class DefaultAuthenticationContext implements AuthenticationContext {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationContext.class);


    /**
     * The Authentication.
     */
    private final Authentication authentication;

    /**
     * Instantiates a new Default authentication context.
     *
     * @param authentication the authentication
     */
    public DefaultAuthenticationContext(final Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }
}
