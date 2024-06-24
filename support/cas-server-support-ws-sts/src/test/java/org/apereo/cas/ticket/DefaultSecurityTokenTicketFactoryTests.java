package org.apereo.cas.ticket;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasWsSecuritySecurityTokenAutoConfiguration;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSecurityTokenTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Tickets")
@ImportAutoConfiguration({
    CasCoreAutoConfiguration.class,
    CasCoreSamlAutoConfiguration.class,
    CasWsSecuritySecurityTokenAutoConfiguration.class
})
class DefaultSecurityTokenTicketFactoryTests extends BaseTicketFactoryTests {

    @Test
    void verifyTicket() throws Throwable {
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
