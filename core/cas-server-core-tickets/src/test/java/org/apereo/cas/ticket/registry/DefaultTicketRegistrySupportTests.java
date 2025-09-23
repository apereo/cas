package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultTicketRegistrySupportTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class)
class DefaultTicketRegistrySupportTests {

    @Test
    void verifyOperation() throws Throwable {
        val registry = new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog(),
                mock(ConfigurableApplicationContext.class));
        val tgt = new MockTicketGrantingTicket("casuser", Map.of("name", List.of("CAS")));
        registry.addTicket(tgt);
        val support = new DefaultTicketRegistrySupport(registry);
        assertNotNull(support.getTicket(tgt.getId()));
        assertNull(support.getTicket(null));
        assertNull(support.getAuthenticationFrom(null));
        assertFalse(support.getPrincipalAttributesFrom(tgt.getId()).isEmpty());

        val authn = CoreAuthenticationTestUtils.getAuthentication("new-authn", Map.of("newAuthN", List.of("CAS-new")));
        support.updateAuthentication(tgt.getId(), authn);
        assertTrue(registry.getTicket(tgt.getId(), TicketGrantingTicket.class)
            .getAuthentication().getAttributes().containsKey("newAuthN"));

    }
}
