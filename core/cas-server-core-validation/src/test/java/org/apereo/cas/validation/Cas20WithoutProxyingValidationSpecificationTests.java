package org.apereo.cas.validation;

import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Simple")
public class Cas20WithoutProxyingValidationSpecificationTests {

    private Cas20WithoutProxyingValidationSpecification validationSpecification;

    @BeforeEach
    public void initialize() {
        this.validationSpecification = new Cas20WithoutProxyingValidationSpecification(mock(ServicesManager.class));
    }

    @Test
    public void verifySatisfiesSpecOfTrue() {
        assertTrue(this.validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(true), new MockHttpServletRequest()));
    }

    @Test
    public void verifyNotSatisfiesSpecOfTrue() {
        this.validationSpecification.setRenew(true);
        assertFalse(this.validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(false), new MockHttpServletRequest()));
    }

    @Test
    public void verifySatisfiesSpecOfFalse() {
        assertTrue(this.validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(false), new MockHttpServletRequest()));
    }

    @Test
    public void verifyDoesNotSatisfiesSpecOfFalse() {
        assertFalse(this.validationSpecification.isSatisfiedBy(
            CoreValidationTestUtils.getAssertion(false, new String[]{"test2"}), new MockHttpServletRequest()));
    }

    @Test
    public void verifySettingRenew() {
        val validation = new Cas20WithoutProxyingValidationSpecification(mock(ServicesManager.class), true);
        assertTrue(validation.isRenew());
    }
}
