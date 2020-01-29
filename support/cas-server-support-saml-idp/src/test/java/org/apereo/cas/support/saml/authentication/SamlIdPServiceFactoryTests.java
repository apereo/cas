package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.support.saml.SamlIdPConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPServiceFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlIdPServiceFactoryTests {
    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlIdPConstants.PROVIDER_ID, "example-sp-entityid");
        val input = new SamlIdPServiceFactory();
        val service = input.createService(request);
        assertNotNull(service);
        assertEquals("example-sp-entityid", service.getId());
    }
}
