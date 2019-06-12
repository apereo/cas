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
@Tag("SAML")
public class DefaultSamlAttributeQueryTicketFactoryTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlAttributeQueryTicketFactory")
    private SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

    @Test
    public void verifyOperation() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val ticketId = samlAttributeQueryTicketFactory.create("ATTR_QUERY",
            getAuthnRequestFor("helloworld"), "https://www.example.org", tgt);
        assertNotNull(ticketId);
        assertNotNull(ticketId.getRelyingParty());
        assertNotNull(ticketId.getObject());
        assertNotNull(ticketId.getExpirationPolicy());
    }
}
