package org.apereo.cas.mock;

import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.ValidationSpecification;

import javax.servlet.http.HttpServletRequest;

/**
 * Class to test the Runtime exception thrown when there is no default
 * constructor on a ValidationSpecification.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class MockValidationSpecification implements ValidationSpecification {

    private boolean test;

    public MockValidationSpecification(final boolean test) {
        this.test = test;
    }

    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        return this.test;
    }
}
