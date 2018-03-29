package org.apereo.cas.support.saml.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;


import static org.junit.Assert.*;

/**
 * Test cases for {@link SamlServiceFactory}
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class SamlServiceFactoryTests {
    @Test
    public void verifyObtainService() {
        final var request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");

        final var factory = new SamlServiceFactory();
        final Service service = factory.createService(request);
        assertEquals("test", service.getId());
    }

    @Test
    public void verifyServiceDoesNotExist() {
        final var request = new MockHttpServletRequest();
        final var factory = new SamlServiceFactory();
        assertNull(factory.createService(request));
    }
}
