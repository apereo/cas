package org.apereo.cas.adaptors.x509.authentication.revocation.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;


/**
 * Implements an unqualified allow policy.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
public class AllowRevocationPolicy implements RevocationPolicy<Void> {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void apply(final Void data) throws GeneralSecurityException {
        logger.info("Continuing since AllowRevocationPolicy is in effect.");
    }
}
