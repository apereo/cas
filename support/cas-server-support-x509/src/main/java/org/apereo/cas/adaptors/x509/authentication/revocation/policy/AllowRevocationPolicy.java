package org.apereo.cas.adaptors.x509.authentication.revocation.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;


/**
 * Implements an unqualified allow policy.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public class AllowRevocationPolicy implements RevocationPolicy<Void> {
    private transient Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * Policy application does nothing to implement unqualfied allow.
     *
     * @param data SHOULD be null; ignored in all cases.
     *
     * @throws GeneralSecurityException Never thrown.
     *
     * @see RevocationPolicy#apply(java.lang.Object)
     */
    @Override
    public void apply(final Void data) throws GeneralSecurityException {
        logger.info("Continuing since AllowRevocationPolicy is in effect.");
    }
}
