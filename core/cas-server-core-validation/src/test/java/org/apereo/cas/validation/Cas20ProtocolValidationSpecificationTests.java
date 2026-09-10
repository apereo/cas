package org.apereo.cas.validation;

import module java.base;
import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CoreValidationTestUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
class Cas20ProtocolValidationSpecificationTests extends BaseCasCoreTests {
    @Autowired
    @Qualifier("casAlwaysSatisfiedProtocolValidationSpecification")
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
        assertTrue(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(true), new MockHttpServletRequest()));
    }

    @Test
    void verifySatisfiesSpecOfFalse2() {
        assertTrue(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(false), new MockHttpServletRequest()));
    }

    @Test
    void verifyRenewIsHonoredFromRequestWithoutRetainingState() {
        val renewRequest = new MockHttpServletRequest();
        renewRequest.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        assertFalse(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(false), renewRequest));
        assertTrue(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(true), renewRequest));

        assertTrue(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(false), new MockHttpServletRequest()));
    }

    @Test
    void verifyRenewIsNotRequestedForNonTrueRequestValues() {
        Stream.of("false", "0", StringUtils.EMPTY, "bogus").forEach(value -> {
            val request = new MockHttpServletRequest();
            request.addParameter(CasProtocolConstants.PARAMETER_RENEW, value);
            assertTrue(validationSpecification.isSatisfiedBy(CoreValidationTestUtils.getAssertion(false), request),
                "Value [%s] must not be understood as a request for renewed authentication".formatted(value));
        });
    }
}
