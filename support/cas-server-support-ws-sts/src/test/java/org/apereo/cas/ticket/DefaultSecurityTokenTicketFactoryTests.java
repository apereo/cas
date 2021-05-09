package org.apereo.cas.ticket;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasWsSecurityTokenTicketCatalogConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenTicketConfiguration;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSecurityTokenTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Tickets")
@Import({
    CasWsSecurityTokenTicketCatalogConfiguration.class,
    CoreWsSecuritySecurityTokenTicketConfiguration.class,
    CasWsSecurityTokenTicketComponentSerializationConfiguration.class
})
public class DefaultSecurityTokenTicketFactoryTests extends BaseTicketFactoryTests {

    @Test
    public void verifyTicket() {
        val securityTokenTicketFactory = (SecurityTokenTicketFactory) ticketFactory.get(SecurityTokenTicket.class);
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgt = new TicketGrantingTicketImpl("TGT-1234567890", originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        val token = securityTokenTicketFactory.create(tgt, "dummy-token".getBytes(StandardCharsets.UTF_8));
        assertNotNull(token);
        val serialized = ticketSerializationManager.serializeTicket(token);
        assertNotNull(serialized);
        val result = ticketSerializationManager.deserializeTicket(serialized, SecurityTokenTicket.class);
        assertNotNull(result);
        assertEquals(result, token);
        
    }
}
