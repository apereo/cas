package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit test for MemcachedTicketRegistry class.
 *
 * @author Middleware Services
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {MemcachedTicketRegistryConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:/memcached.properties"})
public class MemcachedTicketRegistryTests extends AbstractTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry registry;

    public MemcachedTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() throws Exception {
        return Arrays.asList(false, true);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return registry;
    }

    
    @Override
    protected boolean isIterableRegistry() {
        return false;
    }
}
