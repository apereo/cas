package org.apereo.cas.logout;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DescendantTicketsLogoutPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Logout")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = CasCoreLogoutAutoConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.ticket.track-descendant-tickets=true")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DescendantTicketsLogoutPostProcessorTests {
    @Autowired
    @Qualifier("descendantTicketsLogoutPostProcessor")
    private LogoutPostProcessor descendantTicketsLogoutPostProcessor;

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_DESCENDANT_TICKET_TRACKING)
    private TicketTrackingPolicy descendantTicketsTrackingPolicy;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyOperation() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val listOfTickets = new ArrayList<Ticket>();
        val service = RegisteredServiceTestUtils.getService();
        for (var i = 0; i < 5; i++) {
            val st = tgt.grantServiceTicket(service, TicketTrackingPolicy.noOp());
            listOfTickets.add(ticketRegistry.addTicket(st));
        }
        ticketRegistry.updateTicket(tgt);

        listOfTickets.forEach(st -> {
            assertNull(descendantTicketsTrackingPolicy.trackTicket(st, st));
            val trackedEntry = descendantTicketsTrackingPolicy.trackTicket(tgt, st);
            assertNotNull(trackedEntry);
            assertNotNull(descendantTicketsTrackingPolicy.extractTicket(trackedEntry));
        });

        val count = descendantTicketsTrackingPolicy.countTicketsFor(tgt, service);
        assertEquals(5, count);

        descendantTicketsLogoutPostProcessor.handle(tgt);
        listOfTickets.forEach(st -> assertNull(ticketRegistry.getTicket(st.getId())));
        assertNotNull(ticketRegistry.getTicket(tgt.getId()));
    }
}
