package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.registry.jwt.config.JwtTicketRegistryConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link JwtTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RefreshAutoConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreTicketsConfiguration.class,
                JwtTicketRegistryConfiguration.class})
@EnableScheduling
public class JwtTicketRegistryTests {

    @Autowired
    @Qualifier("defaultTicketGrantingTicketFactory")
    private TicketGrantingTicketFactory defaultTicketGrantingTicketFactory;

    @Autowired
    @Qualifier("defaultServiceTicketFactory")
    private ServiceTicketFactory defaultServiceTicketFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("defaultProxyGrantingTicketFactory")
    private ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory;

    @Autowired
    @Qualifier("defaultProxyTicketFactory")
    private ProxyTicketFactory defaultProxyTicketFactory;


    @Test
    public void verifyJwtTicketFactories() {
        final Map attrs = CoreAuthenticationTestUtils.getAttributeRepository()
                .getPerson(CoreAuthenticationTestUtils.CONST_USERNAME)
                .getAttributes();
        final Authentication authn =
                CoreAuthenticationTestUtils.getAuthentication(
                        CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, attrs), attrs);
        final TicketGrantingTicket ticket = defaultTicketGrantingTicketFactory.create(authn);
        assertNotNull(ticket);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));

        final ServiceTicket st = defaultServiceTicketFactory.create(ticket, RegisteredServiceTestUtils.getService(), true);
        assertNotNull(st);

        final ServiceTicket stDecoded = ticketRegistry.getTicket(st.getId(), ServiceTicket.class);
        assertNotNull(stDecoded);
        assertNotNull(stDecoded.getGrantingTicket());
        assertNotNull(stDecoded.getGrantingTicket().getRoot());
        assertEquals(ticket, stDecoded.getGrantingTicket());

        final ProxyGrantingTicket pgt = defaultProxyGrantingTicketFactory.create(st, authn);
        assertNotNull(pgt);

        final ProxyGrantingTicket pgtDecoded = ticketRegistry.getTicket(pgt.getId(), ProxyGrantingTicket.class);
        assertNotNull(pgtDecoded);
        assertNotNull(pgtDecoded.getAuthentication());
        assertNotNull(pgtDecoded.getExpirationPolicy());
        assertNotNull(pgtDecoded.getGrantingTicket());

        final ProxyTicket pt = defaultProxyTicketFactory.create(pgt, RegisteredServiceTestUtils.getService());
        assertNotNull(pt);
        final ProxyTicket ptDecoded = ticketRegistry.getTicket(pt.getId(), ProxyTicket.class);
        assertNotNull(ptDecoded);
        assertNotNull(ptDecoded.getExpirationPolicy());
        assertNotNull(ptDecoded.getGrantingTicket());
    }

}
