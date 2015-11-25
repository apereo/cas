package org.jasig.cas.mock;

import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ValidationSpecification;

/**
 * Class to test the Runtime exception thrown when there is no default
 * constructor on a ValidationSpecification.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class MockValidationSpecification implements ValidationSpecification {

    private final boolean test;

    public MockValidationSpecification(final boolean test) {
        this.test = test;
    }

    @Override
    public boolean isSatisfiedBy(final Assertion assertion) {
        return this.test;
    }
}
