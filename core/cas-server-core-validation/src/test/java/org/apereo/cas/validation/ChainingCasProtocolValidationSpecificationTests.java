package org.apereo.cas.validation;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingCasProtocolValidationSpecificationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("CAS")
class ChainingCasProtocolValidationSpecificationTests {

    private static Assertion getAssertion() {
        val assertion = mock(Assertion.class);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val handlers = (Map) Map.of(new UsernamePasswordCredential(), new SimpleTestUsernamePasswordAuthenticationHandler());
        val authentication = CoreAuthenticationTestUtils.getAuthenticationBuilder(principal, handlers, Map.of()).build();
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);
        when(assertion.getChainedAuthentications()).thenReturn(List.of(authentication));
        return assertion;
    }

    @Test
    void verifyOperationByAny() {
        val servicesManager = mock(ServicesManager.class);
        val chain = new ChainingCasProtocolValidationSpecification(true);
        chain.addSpecifications(new DefaultCasProtocolValidationSpecification(servicesManager, input -> true),
            new DefaultCasProtocolValidationSpecification(servicesManager, input -> input.getChainedAuthentications().size() == 1));
        assertEquals(2, chain.size());
        chain.reset();
        assertTrue(chain.isSatisfiedBy(getAssertion(), new MockHttpServletRequest()));
    }

    @Test
    void verifyOperationByAll() {
        val servicesManager = mock(ServicesManager.class);
        val chain = new ChainingCasProtocolValidationSpecification(false);
        chain.addSpecifications(new DefaultCasProtocolValidationSpecification(servicesManager, input -> true));
        chain.reset();
        assertTrue(chain.isSatisfiedBy(getAssertion(), new MockHttpServletRequest()));
    }

    @Test
    void verifyRenewIsResolvedPerRequestAndLeavesNoState() {
        val servicesManager = mock(ServicesManager.class);
        val chain = new ChainingCasProtocolValidationSpecification(false);
        val specification = new DefaultCasProtocolValidationSpecification(servicesManager, input -> true);
        chain.addSpecifications(specification);

        val renewRequest = new MockHttpServletRequest();
        renewRequest.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        assertFalse(chain.isSatisfiedBy(getAssertion(), renewRequest));

        assertFalse(specification.isRenew(), "Evaluating the chain must not leave renew state behind on a chained specification");
        assertTrue(chain.isSatisfiedBy(getAssertion(), new MockHttpServletRequest()));
    }

    @Test
    void verifyRenewSetProgrammaticallyIsForwardedToChainedSpecifications() {
        val servicesManager = mock(ServicesManager.class);
        val chain = new ChainingCasProtocolValidationSpecification(false);
        val specification = new DefaultCasProtocolValidationSpecification(servicesManager, input -> true);
        chain.addSpecifications(specification);

        chain.setRenew(true);
        assertTrue(specification.isRenew());
        assertFalse(chain.isSatisfiedBy(getAssertion(), new MockHttpServletRequest()));

        chain.reset();
        assertTrue(chain.isSatisfiedBy(getAssertion(), new MockHttpServletRequest()));
    }
}
