package org.apereo.cas.validation;

import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class Cas20ProtocolValidationSpecification extends AbstractCasProtocolValidationSpecification {
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
