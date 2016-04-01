package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Implements an unqualified allow policy.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
@Component("allowRevocationPolicy")
public final class AllowRevocationPolicy implements RevocationPolicy<Void> {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * Policy application does nothing to implement unqualfied allow.
     *
     * @param data SHOULD be null; ignored in all cases.
     *
     * @throws GeneralSecurityException Never thrown.
     *
     * @see org.jasig.cas.adaptors.x509.authentication.handler.support.RevocationPolicy#apply(java.lang.Object)
     */
    @Override
    public void apply(final Void data) throws GeneralSecurityException {
        logger.info("Continuing since AllowRevocationPolicy is in effect.");
    }
}
