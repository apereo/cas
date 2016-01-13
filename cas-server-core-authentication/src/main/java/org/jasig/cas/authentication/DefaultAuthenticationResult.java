package org.jasig.cas.authentication;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link DefaultAuthenticationResult} represents a concrete implementation of {@link AuthenticationResult}.
 * It acts as a carrier for the finalized primary authentications established during processing of authentication events
 * (posiblly multi-transactional) by CAS' authentication subsystem.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class DefaultAuthenticationResult implements AuthenticationResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationResult.class);
    private static final long serialVersionUID = 8454900425245262824L;

    private final Authentication authentication;

    private final Service service;

    /**
     * Instantiates a new Default authentication result.
     *
     * @param authentication the authentication
     * @param service        the service
     */
    public DefaultAuthenticationResult(final Authentication authentication, final Service service) {
        this.authentication = authentication;
        this.service = service;
    }

    /**
     * Instantiates a new Default authentication result.
     *
     * @param authentication the authentication
     */
    public DefaultAuthenticationResult(final Authentication authentication) {
        this(authentication, null);
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public Principal getPrincipal() {
        return getAuthentication().getPrincipal();
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
