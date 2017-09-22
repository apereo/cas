package org.apereo.cas.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation specification for the CAS 2.0 protocol. This specification extends
 * the Cas10ProtocolValidationSpecification, checking for the presence of
 * renew=true and if requested, succeeding only if ticket validation is
 * occurring from a new login.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class Cas20ProtocolValidationSpecification extends AbstractCasProtocolValidationSpecification {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cas20ProtocolValidationSpecification.class);
    
    /**
     * Instantiates a new cas20 protocol validation specification.
     */
    public Cas20ProtocolValidationSpecification() {
        super();
    }

    /**
     * Instantiates a new cas20 protocol validation specification.
     *
     * @param renew the renew
     */
    public Cas20ProtocolValidationSpecification(final boolean renew) {
        super(renew);
    }

    @Override
    protected boolean isSatisfiedByInternal(final Assertion assertion) {
        LOGGER.debug("Assertion is always satisfied");
        return true;
    }
}
