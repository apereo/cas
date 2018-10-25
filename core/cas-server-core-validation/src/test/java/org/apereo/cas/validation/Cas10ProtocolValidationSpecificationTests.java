package org.apereo.cas.validation;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class Cas10ProtocolValidationSpecificationTests {

    @Test
    public void verifyRenewGettersAndSettersFalse() {
        val s = new Cas10ProtocolValidationSpecification();
        s.setRenew(false);
        assertFalse(s.isRenew());
    }

    @Test
    public void verifyRenewGettersAndSettersTrue() {
        val s = new Cas10ProtocolValidationSpecification();
        s.setRenew(true);
        assertTrue(s.isRenew());
    }

    @Test
    public void verifyRenewAsTrueAsConstructor() {
        assertTrue(new Cas10ProtocolValidationSpecification(true).isRenew());
    }

    @Test
    public void verifyRenewAsFalseAsConstructor() {
        assertFalse(new Cas10ProtocolValidationSpecification(false).isRenew());
    }

    @Test
    public void verifySatisfiesSpecOfTrue() {
        assertTrue(new Cas10ProtocolValidationSpecification(true).isSatisfiedBy(CoreValidationTestUtils.getAssertion(true),
            new MockHttpServletRequest()));
    }

    @Test
    public void verifyNotSatisfiesSpecOfTrue() {
        assertFalse(new Cas10ProtocolValidationSpecification(true).isSatisfiedBy(CoreValidationTestUtils.getAssertion(false),
            new MockHttpServletRequest()));
    }

    @Test
    public void verifySatisfiesSpecOfFalse() {
        assertTrue(new Cas10ProtocolValidationSpecification(false).isSatisfiedBy(CoreValidationTestUtils.getAssertion(true),
            new MockHttpServletRequest()));
    }

    @Test
    public void verifySatisfiesSpecOfFalse2() {
        assertTrue(new Cas10ProtocolValidationSpecification(false).isSatisfiedBy(CoreValidationTestUtils.getAssertion(false),
            new MockHttpServletRequest()));
    }

}
