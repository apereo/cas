package org.jasig.cas.support.saml.authentication.principal;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;


import static org.junit.Assert.*;

/**
 * Test cases for {@link SamlServiceFactory}
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlServiceFactoryTests {
    @Test
    public void verifyObtainService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");

        final SamlServiceFactory factory = new SamlServiceFactory();
        final Service service = factory.createService(request);
        assertEquals("test", service.getId());
    }

    @Test
    public void verifyServiceDoesNotExist() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final SamlServiceFactory factory = new SamlServiceFactory();
        assertNull(factory.createService(request));
    }
}
