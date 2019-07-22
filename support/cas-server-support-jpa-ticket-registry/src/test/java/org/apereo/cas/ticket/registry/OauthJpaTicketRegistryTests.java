package org.apereo.cas.ticket.registry;

import lombok.val;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.*;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit test for {@link JpaTicketRegistry} class in context of OAuth 2.0.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SpringBootTest(classes = {
    JpaTicketRegistryTicketCatalogConfiguration.class,
    JpaTicketRegistryConfiguration.class,
    CasOAuthConfiguration.class,
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreWebConfiguration.class,
    OAuthProtocolTicketCatalogConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasWsSecurityTokenTicketCatalogConfiguration.class
})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@Transactional(transactionManager = "ticketTransactionManager", isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
@ResourceLock("oauth-jpa-tickets")
@Tag("OAuth")
public class OauthJpaTicketRegistryTests {

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory ticketFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private OAuthCodeFactory oAuthCodeFactory;

    @AfterEach
    public void cleanup() {
        ticketRegistry.deleteAll();
    }

    @Test
    public void verifyLogoutCascades() {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(), TicketGrantingTicket.class);
        this.ticketRegistry.addTicket(tgt);

        val oAuthCode = oAuthCodeFactory.create(RegisteredServiceTestUtils.getService(),
                originalAuthn, tgt, Collections.emptySet(), "challenge", "challenge_method",
                "client_id", Collections.emptyMap());

        this.ticketRegistry.addTicket(oAuthCode);

        assertNotNull(this.ticketRegistry.getTicket(oAuthCode.getId()));
        this.ticketRegistry.deleteTicket(tgt.getId());
        assertNull(this.ticketRegistry.getTicket(oAuthCode.getId()));
    }
}
