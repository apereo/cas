package org.apereo.cas.validation;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingCasProtocolValidationSpecificationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("Simple")
public class ChainingCasProtocolValidationSpecificationTests {

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
    public void verifyOperationByAny() {
        val servicesManager = mock(ServicesManager.class);
        val chain = new ChainingCasProtocolValidationSpecification(true);
        chain.addSpecifications(new Cas20ProtocolValidationSpecification(servicesManager),
            new Cas10ProtocolValidationSpecification(servicesManager));
        assertEquals(2, chain.size());
        chain.reset();
        assertTrue(chain.isSatisfiedBy(getAssertion(), new MockHttpServletRequest()));
    }

    @Test
    public void verifyOperationByAll() {
        val servicesManager = mock(ServicesManager.class);
        val chain = new ChainingCasProtocolValidationSpecification(false);
        chain.addSpecifications(new Cas20ProtocolValidationSpecification(servicesManager));
        chain.reset();
        assertTrue(chain.isSatisfiedBy(getAssertion(), new MockHttpServletRequest()));
    }
}
