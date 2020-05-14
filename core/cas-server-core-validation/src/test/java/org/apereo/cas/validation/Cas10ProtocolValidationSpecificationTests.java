package org.apereo.cas.validation;

import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(MockitoExtension.class)
@Tag("Simple")
public class Cas10ProtocolValidationSpecificationTests {
    @Mock
    private ServicesManager servicesManager;

    @Test
    public void verifyRenewGettersAndSettersFalse() {
        val s = new Cas10ProtocolValidationSpecification(servicesManager);
        s.setRenew(false);
        assertFalse(s.isRenew());
    }

    @Test
    public void verifyRenewGettersAndSettersTrue() {
        val s = new Cas10ProtocolValidationSpecification(servicesManager);
        s.setRenew(true);
        assertTrue(s.isRenew());
    }

    @Test
    public void verifyRenewAsTrueAsConstructor() {
        assertTrue(new Cas10ProtocolValidationSpecification(servicesManager, true).isRenew());
    }

    @Test
    public void verifyRenewAsFalseAsConstructor() {
        assertFalse(new Cas10ProtocolValidationSpecification(servicesManager, false).isRenew());
    }

    @Test
    public void verifySatisfiesSpecOfTrue() {
        assertTrue(new Cas10ProtocolValidationSpecification(servicesManager, true).isSatisfiedBy(CoreValidationTestUtils.getAssertion(true),
            new MockHttpServletRequest()));
    }

    @Test
    public void verifyNotSatisfiesSpecOfTrue() {
        assertFalse(new Cas10ProtocolValidationSpecification(servicesManager, true).isSatisfiedBy(CoreValidationTestUtils.getAssertion(false),
            new MockHttpServletRequest()));
    }

    @Test
    public void verifySatisfiesSpecOfFalse() {
        assertTrue(new Cas10ProtocolValidationSpecification(servicesManager, false).isSatisfiedBy(CoreValidationTestUtils.getAssertion(true),
            new MockHttpServletRequest()));
    }

    @Test
    public void verifySatisfiesSpecOfFalse2() {
        assertTrue(new Cas10ProtocolValidationSpecification(servicesManager, false).isSatisfiedBy(CoreValidationTestUtils.getAssertion(false),
            new MockHttpServletRequest()));
    }

}
