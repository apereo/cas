package org.apereo.cas.validation;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class Cas20ProtocolValidationSpecificationTests {

    @Test
    public void verifyRenewGettersAndSettersFalse() {
        final Cas20ProtocolValidationSpecification s = new Cas20ProtocolValidationSpecification();
        s.setRenew(false);
        assertFalse(s.isRenew());
    }

    @Test
    public void verifyRenewGettersAndSettersTrue() {
        final Cas20ProtocolValidationSpecification s = new Cas20ProtocolValidationSpecification();
        s.setRenew(true);
        assertTrue(s.isRenew());
    }

    @Test
    public void verifyRenewAsTrueAsConstructor() {
        assertTrue(new Cas20ProtocolValidationSpecification(true).isRenew());
    }

    @Test
    public void verifyRenewAsFalseAsConstructor() {
        assertFalse(new Cas20ProtocolValidationSpecification(false).isRenew());
    }

    @Test
    public void verifySatisfiesSpecOfTrue() {
        assertTrue(new Cas20ProtocolValidationSpecification(true).isSatisfiedBy(CoreValidationTestUtils.getAssertion(true),
                new MockHttpServletRequest()));
    }

    @Test
    public void verifyNotSatisfiesSpecOfTrue() {
        assertFalse(new Cas20ProtocolValidationSpecification(true).isSatisfiedBy(CoreValidationTestUtils.getAssertion(false),
                new MockHttpServletRequest()));
    }

    @Test
    public void verifySatisfiesSpecOfFalse() {
        assertTrue(new Cas20ProtocolValidationSpecification(false).isSatisfiedBy(CoreValidationTestUtils.getAssertion(true),
                new MockHttpServletRequest()));
    }

    @Test
    public void verifySatisfiesSpecOfFalse2() {
        assertTrue(new Cas20ProtocolValidationSpecification(false).isSatisfiedBy(CoreValidationTestUtils.getAssertion(false),
                new MockHttpServletRequest()));
    }
}
