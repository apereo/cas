package org.apereo.cas.adaptors.x509.authentication.revocation.policy;

import module java.base;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements an unqualified allow policy, applicable to unavailable and expired CRL data alike.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@Slf4j
public class AllowRevocationPolicy implements RevocationPolicy<Object> {
    @Override
    public void apply(final Object data) {
        LOGGER.debug("Continuing since [{}] is in effect.", getClass().getSimpleName());
    }
}
