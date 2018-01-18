package org.apereo.cas.authentication;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;
import lombok.ToString;
import lombok.Setter;

/**
 * The {@link DefaultAuthenticationResult} represents a concrete implementation of {@link AuthenticationResult}.
 * It acts as a carrier for the finalized primary authentications established during processing of authentication events
 * (possibly multi-transactional) by CAS' authentication subsystem.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@ToString
@Setter
@Getter
public class DefaultAuthenticationResult implements AuthenticationResult {

    private static final long serialVersionUID = 8454900425245262824L;

    private boolean credentialProvided;

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
    
}
