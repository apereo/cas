package org.apereo.cas.ticket.query;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSamlAttributeQueryTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAML2")
class DefaultSamlAttributeQueryTicketFactoryTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlAttributeQueryTicketFactory")
    private SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

    @Test
    void verifyOperation() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val ticketId = samlAttributeQueryTicketFactory.create("ATTR_QUERY",
            getAuthnRequestFor("helloworld"), "https://www.example.org", tgt);
        assertNotNull(ticketId);
        assertNull(ticketId.getTicketGrantingTicket());
        assertNotNull(ticketId.getPrefix());
        assertNotNull(ticketId.getAuthentication());
        assertNotNull(ticketId.getObject());
        assertNotNull(ticketId.getRelyingParty());
        assertNotNull(ticketId.getExpirationPolicy());
    }
}
