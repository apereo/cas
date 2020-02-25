package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The {@link DefaultAuthenticationResult} represents a concrete implementation of {@link AuthenticationResult}.
 * It acts as a carrier for the finalized primary authentications established during processing of authentication events
 * (possibly multi-transactional) by CAS' authentication subsystem.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@ToString
@Setter
@Getter
@RequiredArgsConstructor
public class DefaultAuthenticationResult implements AuthenticationResult {
    private static final long serialVersionUID = 8454900425245262824L;

    private final Authentication authentication;

    private final Service service;

    private boolean credentialProvided;
}
