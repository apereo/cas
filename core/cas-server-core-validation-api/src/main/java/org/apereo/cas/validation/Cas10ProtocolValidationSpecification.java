package org.apereo.cas.validation;

import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Validation specification for the CAS 1.0 protocol. This specification checks
 * for the presence of renew=true and if requested, succeeds only if ticket
 * validation is occurring from a new login. Additionally, validation will fail
 * if passed a proxy ticket.
 *
 * @author Scott Battaglia
 * @author Drew Mazurek
 * @since 3.0.0
 */
@Slf4j
public class Cas10ProtocolValidationSpecification extends AbstractCasProtocolValidationSpecification {
    public Cas10ProtocolValidationSpecification(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    public Cas10ProtocolValidationSpecification(final ServicesManager servicesManager, final boolean renew) {
        super(servicesManager, renew);
    }


    @Override
    protected boolean isSatisfiedByInternal(final Assertion assertion) {
        LOGGER.trace("Number of chained authentications in the assertion [{}]", assertion.getChainedAuthentications().size());
        return assertion.getChainedAuthentications().size() == 1;
    }
}
