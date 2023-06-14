package org.apereo.cas.validation;

import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CoreValidationTestUtils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("CAS")
class Cas20WithoutProxyingValidationSpecificationTests extends BaseCasCoreTests {
    @Autowired
    @Qualifier("casSingleAuthenticationProtocolValidationSpecification")
    private CasProtocolValidationSpecification validationSpecification;


    @Test
    void verifySatisfiesSpecOfTrue() {
        assertTrue(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(true), new MockHttpServletRequest()));
    }

    @Test
    void verifyNotSatisfiesSpecOfTrue() {
        validationSpecification.setRenew(true);
        assertFalse(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(false), new MockHttpServletRequest()));
    }

    @Test
    void verifySatisfiesSpecOfFalse() {
        assertTrue(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(false), new MockHttpServletRequest()));
    }

    @Test
    void verifyDoesNotSatisfiesSpecOfFalse() {
        assertFalse(validationSpecification.isSatisfiedBy(
            CoreValidationTestUtils.getAssertion(false, new String[]{"test2"}), new MockHttpServletRequest()));
    }
}
