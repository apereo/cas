package org.jasig.cas.validation;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class Cas20WithoutProxyingValidationSpecificationTests {

    private Cas20WithoutProxyingValidationSpecification validationSpecification;

    @Before
    public void setUp() throws Exception {
        this.validationSpecification = new Cas20WithoutProxyingValidationSpecification();
    }

    @Test
    public void verifySatisfiesSpecOfTrue() {
        assertTrue(this.validationSpecification.isSatisfiedBy(TestUtils.getAssertion(true)));
    }

    @Test
    public void verifyNotSatisfiesSpecOfTrue() {
        this.validationSpecification.setRenew(true);
        assertFalse(this.validationSpecification.isSatisfiedBy(TestUtils.getAssertion(false)));
    }

    @Test
    public void verifySatisfiesSpecOfFalse() {
        assertTrue(this.validationSpecification.isSatisfiedBy(TestUtils.getAssertion(false)));
    }

    @Test
    public void verifyDoesNotSatisfiesSpecOfFalse() {
        assertFalse(this.validationSpecification.isSatisfiedBy(TestUtils.getAssertion(false, new String[] {"test2"})));
    }

    @Test
    public void verifySettingRenew() {
        final Cas20WithoutProxyingValidationSpecification validation = new Cas20WithoutProxyingValidationSpecification(
                true);
        assertTrue(validation.isRenew());
    }
}
