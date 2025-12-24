package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

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
@SuperBuilder
public class DefaultAuthenticationResult implements AuthenticationResult {
    @Serial
    private static final long serialVersionUID = 8454900425245262824L;

    private final Authentication authentication;

    @Nullable
    private final Service service;

    private boolean credentialProvided;
}
