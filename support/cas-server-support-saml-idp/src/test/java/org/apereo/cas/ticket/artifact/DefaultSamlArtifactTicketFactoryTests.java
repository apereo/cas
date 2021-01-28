package org.apereo.cas.ticket.artifact;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSamlArtifactTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML")
public class DefaultSamlArtifactTicketFactoryTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlArtifactTicketFactory")
    private SamlArtifactTicketFactory samlArtifactTicketFactory;

    @Test
    public void verifyOperation() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val ticketId = samlArtifactTicketFactory.create(UUID.randomUUID().toString(), tgt.getAuthentication(),
            tgt, casProperties.getAuthn().getSamlIdp().getCore().getEntityId(),
            "https://www.example.org", getAuthnRequestFor("helloworld"));
        assertNotNull(ticketId);
        assertNotNull(ticketId.getPrefix());
        assertNotNull(ticketId.getTicketGrantingTicket());
        assertNotNull(ticketId.getObject());
        assertNotNull(ticketId.getRelyingPartyId());
        assertNotNull(ticketId.getExpirationPolicy());
        assertTrue(ticketId.isFromNewLogin());
        assertThrows(UnsupportedOperationException.class,
            () -> ticketId.grantProxyGrantingTicket("id",
                tgt.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE));
    }
}
