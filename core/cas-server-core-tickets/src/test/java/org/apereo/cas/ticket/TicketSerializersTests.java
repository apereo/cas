package org.apereo.cas.ticket;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketSerializersTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = BaseTicketFactoryTests.SharedTestConfiguration.class)
@Tag("Tickets")
class TicketSerializersTests {

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory defaultTicketFactory;

    @Autowired
    @Qualifier(TicketSerializationManager.BEAN_NAME)
    private TicketSerializationManager ticketSerializationManager;

    @Test
    void verifyTicketGrantingTicketSerialization() throws Throwable {
        val factory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);
        verifySerialization(ticket);
    }

    @Test
    void verifyTransientSessionTicketSerialization() throws Throwable {
        val factory = (TransientSessionTicketFactory) this.defaultTicketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService(), CollectionUtils.wrap("key", "value"));
        verifySerialization(ticket);
    }

    @Test
    void verifyServiceTicketSerialization() throws Throwable {
        val tgtFactory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);

        val factory = (ServiceTicketFactory) this.defaultTicketFactory.get(ServiceTicket.class);
        val ticket = factory.create(tgt, RegisteredServiceTestUtils.getService(), true, ServiceTicket.class);
        verifySerialization(ticket);
    }

    @Test
    void verifyProxyGrantingTicketSerialization() throws Throwable {
        val tgtFactory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);

        val stFactory = (ServiceTicketFactory) this.defaultTicketFactory.get(ServiceTicket.class);
        val st = stFactory.create(tgt, RegisteredServiceTestUtils.getService(), true, ServiceTicket.class);

        val pgtFactory = (ProxyGrantingTicketFactory) this.defaultTicketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(st, tgt.getAuthentication(), ProxyGrantingTicket.class);

        verifySerialization(pgt);
    }

    @Test
    void verifyProxyTicketSerialization() throws Throwable {
        val tgtFactory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);

        val stFactory = (ServiceTicketFactory) this.defaultTicketFactory.get(ServiceTicket.class);
        val st = stFactory.create(tgt, RegisteredServiceTestUtils.getService(), true, ServiceTicket.class);

        val pgtFactory = (ProxyGrantingTicketFactory) this.defaultTicketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(st, tgt.getAuthentication(), ProxyGrantingTicket.class);

        val ptFactory = (ProxyTicketFactory) this.defaultTicketFactory.get(ProxyTicket.class);
        val pt = ptFactory.create(pgt, st.getService(), ProxyTicket.class);

        verifySerialization(pt);
    }

    private void verifySerialization(final Ticket ticket) {
        val serialized = ticketSerializationManager.serializeTicket(ticket);
        assertNotNull(serialized);
        val deserialized = ticketSerializationManager.deserializeTicket(serialized, ticket.getClass());
        assertNotNull(deserialized);
        assertEquals(deserialized, ticket);
    }
}
