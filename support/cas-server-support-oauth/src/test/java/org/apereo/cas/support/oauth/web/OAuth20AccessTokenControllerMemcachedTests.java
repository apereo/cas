package org.apereo.cas.support.oauth.web;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasOAuthAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuthComponentSerializationConfiguration;
import org.apereo.cas.config.CasOAuthConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistry;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link OAuth20AccessTokenControllerMemcachedTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                CasCoreAuthenticationConfiguration.class,
                CasCoreServicesAuthenticationConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasOAuth20TestAuthenticationEventExecutionPlanConfiguration.class,
                CasDefaultServiceTicketIdGeneratorsConfiguration.class,
                CasCoreTicketIdGeneratorsConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasOAuthConfiguration.class,
                CasCoreTicketsConfiguration.class,
                MemcachedTicketRegistryConfiguration.class,
                CasCoreConfiguration.class,
                CasCookieConfiguration.class,
                CasOAuthComponentSerializationConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                CasOAuthAuthenticationServiceSelectionStrategyConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
                CasCoreComponentSerializationConfiguration.class,
                CasOAuth20TestAuthenticationEventExecutionPlanConfiguration.class,
                CasCoreUtilSerializationConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                AbstractOAuth20Tests.OAuthTestConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasCoreWebConfiguration.class})
@TestPropertySource(locations = {"classpath:/memcached-oauth.properties"})
public class OAuth20AccessTokenControllerMemcachedTests extends AbstractOAuth20Tests {
    @Before
    public void setUp() {
        clearAllServices();
    }

    @Test
    public void verifyTicketRegistry() {
        assertTrue(this.ticketRegistry instanceof MemcachedTicketRegistry);
    }
    
    @Test
    public void verifyOAuthCodeIsAddedToMemcached() {
        final Principal p = createPrincipal();
        final OAuthCode code = addCode(p, addRegisteredService());
        final Ticket ticket = this.ticketRegistry.getTicket(code.getId(), OAuthCode.class);
        assertNotNull(ticket);
    }
}
