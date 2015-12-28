package org.jasig.cas.authentication;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Service;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationContext.class);

    private final Authentication authentication;

    private final Service service;

    /**
     * Instantiates a new Default authentication context.
     *
     * @param authentication the authentication
     * @param service        the service
     */
    public DefaultAuthenticationContext(final Authentication authentication, final Service service) {
        this.authentication = authentication;
        this.service = service;
    }

    /**
     * Instantiates a new Default authentication context.
     *
     * @param authentication the authentication
     */
    public DefaultAuthenticationContext(final Authentication authentication) {
        this(authentication, null);
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public Service getService() {
        return this.service;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("authentication", authentication)
                .toString();
    }
}
