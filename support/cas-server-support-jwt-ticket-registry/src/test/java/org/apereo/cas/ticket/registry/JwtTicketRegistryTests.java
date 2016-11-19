package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.registry.config.JwtTicketRegistryConfiguration;
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

    @Test
    public void verifyTicketGrantingTicketAsJwt() {
        final Map attrs = CoreAuthenticationTestUtils.getAttributeRepository()
                .getPerson(CoreAuthenticationTestUtils.CONST_USERNAME)
                .getAttributes();
        final Authentication authn =
                CoreAuthenticationTestUtils.getAuthentication(
                        CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, attrs), attrs);
        final TicketGrantingTicket ticket = defaultTicketGrantingTicketFactory.create(authn);
        assertNotNull(ticket);
    }
}
