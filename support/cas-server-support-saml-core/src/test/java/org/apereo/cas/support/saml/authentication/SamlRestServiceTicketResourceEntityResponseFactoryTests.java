package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRestServiceTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlRestServiceTicketResourceEntityResponseFactoryTests {
    @Test
    public void verifyOperation() {
        val factory = new SamlRestServiceTicketResourceEntityResponseFactory(new DefaultUniqueTicketIdGenerator());
        assertEquals(0, factory.getOrder());

        val service = new SamlService();
        service.setId("https://saml.example.org");
        assertTrue(factory.supports(service, CoreAuthenticationTestUtils.getAuthenticationResult()));

        assertNotNull(factory.build(new MockTicketGrantingTicket("casuser").getId(),
            service, CoreAuthenticationTestUtils.getAuthenticationResult()));
    }
}
