package org.jasig.cas.validation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
@Component("cas10ProtocolValidationSpecification")
@Scope(value = "prototype")
public final class Cas10ProtocolValidationSpecification extends AbstractCasProtocolValidationSpecification {

    /**
     * Instantiates a new cas10 protocol validation specification.
     */
    public Cas10ProtocolValidationSpecification() {
        super();
    }

    /**
     * Instantiates a new cas10 protocol validation specification.
     *
     * @param renew the renew
     */
    public Cas10ProtocolValidationSpecification(final boolean renew) {
        super(renew);
    }

    @Override
    protected boolean isSatisfiedByInternal(final Assertion assertion) {
        return (assertion.getChainedAuthentications().size() == 1);
    }
}
