package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link UniquePrincipalAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreTicketCatalogConfiguration.class
})
@DirtiesContext
public class UniquePrincipalAuthenticationPolicyTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;
    
    @Test
    public void verifyPolicyIsGoodUserNotFound() throws Exception {
        this.ticketRegistry.deleteAll();
        final UniquePrincipalAuthenticationPolicy p = new UniquePrincipalAuthenticationPolicy(this.ticketRegistry);
        assertTrue(p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser")));
    }

    @Test
    public void verifyPolicyFailsUserFoundOnce() throws Exception {
        this.ticketRegistry.deleteAll();
        this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TGT-1", CoreAuthenticationTestUtils.getAuthentication("casuser"), 
                new NeverExpiresExpirationPolicy()));
        final UniquePrincipalAuthenticationPolicy p = new UniquePrincipalAuthenticationPolicy(this.ticketRegistry);
        assertFalse(p.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication("casuser")));
    }
}
