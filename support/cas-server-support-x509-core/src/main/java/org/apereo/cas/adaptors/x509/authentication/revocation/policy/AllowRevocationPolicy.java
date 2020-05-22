package org.apereo.cas.adaptors.x509.authentication.revocation.policy;

import lombok.extern.slf4j.Slf4j;

/**
 * Implements an unqualified allow policy.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@Slf4j
public class AllowRevocationPolicy implements RevocationPolicy<Void> {

    @Override
    public void apply(final Void data) {
        LOGGER.info("Continuing since AllowRevocationPolicy is in effect.");
    }
}
