package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSchedulingConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * This is {@link JpaTicketRegistryCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
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
    JpaTicketRegistryTicketCatalogConfiguration.class,
    JpaTicketRegistryConfiguration.class,
    CasCoreTicketsSchedulingConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@Slf4j
@Transactional(transactionManager = "ticketTransactionManager", isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
public class JpaTicketRegistryCleanerTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("ticketRegistryCleaner")
    private TicketRegistryCleaner ticketRegistryCleaner;

    @Test
    public void verifyOperation() {
        final TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl("TGT-123456",
            RegisteredServiceTestUtils.getAuthentication(), new AlwaysExpiresExpirationPolicy());
        final ServiceTicket st = tgt.grantServiceTicket("ST-12345",
            RegisteredServiceTestUtils.getService(),
            new AlwaysExpiresExpirationPolicy(), true, false);
        ticketRegistry.addTicket(tgt);
        ticketRegistry.addTicket(st);
        ticketRegistryCleaner.clean();
        assertTrue(this.ticketRegistry.getTickets().isEmpty());
    }
}
