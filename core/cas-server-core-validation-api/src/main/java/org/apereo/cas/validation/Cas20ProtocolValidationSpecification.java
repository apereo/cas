package org.apereo.cas.validation;

import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Validation specification for the CAS 2.0 protocol. This specification extends
 * the Cas10ProtocolValidationSpecification, checking for the presence of
 * renew=true and if requested, succeeding only if ticket validation is
 * occurring from a new login.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public class Cas20ProtocolValidationSpecification extends AbstractCasProtocolValidationSpecification {
    public Cas20ProtocolValidationSpecification(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    public Cas20ProtocolValidationSpecification(final ServicesManager servicesManager, final boolean renew) {
        super(servicesManager, renew);
    }

    @Override
    protected boolean isSatisfiedByInternal(final Assertion assertion) {
        LOGGER.trace("Assertion is always satisfied");
        return true;
    }
}
