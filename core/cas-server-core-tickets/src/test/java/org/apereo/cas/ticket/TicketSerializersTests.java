package org.apereo.cas.ticket;

import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.Assert.*;

/**
 * This is {@link TicketSerializersTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class
})
public class TicketSerializersTests {
    @Autowired
    @Qualifier("defaultTicketFactory")
    protected TicketFactory defaultTicketFactory;

    @Test
    public void verifyTicketGrantingTicketSerialization() {
        val factory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getAuthentication(), TicketGrantingTicket.class);
        verifySerialization(ticket);
    }

    @Test
    public void verifyServiceTicketSerialization() {
        val tgtFactory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(), TicketGrantingTicket.class);

        val factory = (ServiceTicketFactory) this.defaultTicketFactory.get(ServiceTicket.class);
        val ticket = factory.create(tgt, RegisteredServiceTestUtils.getService(), true, ServiceTicket.class);
        verifySerialization(ticket);
    }

    @Test
    public void verifyProxyGrantingTicketSerialization() {
        val tgtFactory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(), TicketGrantingTicket.class);

        val stFactory = (ServiceTicketFactory) this.defaultTicketFactory.get(ServiceTicket.class);
        val st = stFactory.create(tgt, RegisteredServiceTestUtils.getService(), true, ServiceTicket.class);

        val pgtFactory = (ProxyGrantingTicketFactory) this.defaultTicketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(st, tgt.getAuthentication(), ProxyGrantingTicket.class);

        verifySerialization(pgt);
    }

    @Test
    public void verifyProxyTicketSerialization() {
        val tgtFactory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(), TicketGrantingTicket.class);

        val stFactory = (ServiceTicketFactory) this.defaultTicketFactory.get(ServiceTicket.class);
        val st = stFactory.create(tgt, RegisteredServiceTestUtils.getService(), true, ServiceTicket.class);

        val pgtFactory = (ProxyGrantingTicketFactory) this.defaultTicketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(st, tgt.getAuthentication(), ProxyGrantingTicket.class);

        val ptFactory = (ProxyTicketFactory) this.defaultTicketFactory.get(ProxyTicket.class);
        val pt = ptFactory.create(pgt, st.getService(), ProxyTicket.class);

        verifySerialization(pt);
    }

    private static void verifySerialization(final Ticket ticket) {
        val serialized = BaseTicketSerializers.serializeTicket(ticket);
        assertNotNull(serialized);
        val deserialized = BaseTicketSerializers.deserializeTicket(serialized, ticket.getClass());
        assertNotNull(deserialized);
    }
}
