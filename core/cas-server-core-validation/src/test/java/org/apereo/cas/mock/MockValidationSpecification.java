package org.apereo.cas.mock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.CasProtocolValidationSpecification;

import javax.servlet.http.HttpServletRequest;

/**
 * Class to test the Runtime exception thrown when there is no default
 * constructor on a CasProtocolValidationSpecification.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
@AllArgsConstructor
public class MockValidationSpecification implements CasProtocolValidationSpecification {
    private final boolean test;

    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        return this.test;
    }
}
