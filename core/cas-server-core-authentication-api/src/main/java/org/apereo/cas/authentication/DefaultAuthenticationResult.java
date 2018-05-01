package org.apereo.cas.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;

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
@RequiredArgsConstructor
public class DefaultAuthenticationResult implements AuthenticationResult {
    private static final long serialVersionUID = 8454900425245262824L;

    private boolean credentialProvided;

    private final Authentication authentication;
    private final Service service;
}
