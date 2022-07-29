package org.apereo.cas.validation;

import org.apereo.cas.services.ServicesManager;

import java.util.function.Function;

/**
 * This is {@link DefaultCasProtocolValidationSpecification}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class DefaultCasProtocolValidationSpecification extends AbstractCasProtocolValidationSpecification {
    private final Function<Assertion, Boolean> evaluator;

    public DefaultCasProtocolValidationSpecification(final ServicesManager servicesManager,
                                                     final Function<Assertion, Boolean> evaluator) {
        super(servicesManager, false);
        this.evaluator = evaluator;
    }

    @Override
    protected boolean isSatisfiedByInternal(final Assertion assertion) {
        return evaluator.apply(assertion);
    }
}
