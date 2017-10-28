package org.apereo.cas.adaptors.x509.authentication.revocation.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements an unqualified allow policy.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
public class AllowRevocationPolicy implements RevocationPolicy<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllowRevocationPolicy.class);
    
    @Override
    public void apply(final Void data) {
        LOGGER.info("Continuing since AllowRevocationPolicy is in effect.");
    }
}
