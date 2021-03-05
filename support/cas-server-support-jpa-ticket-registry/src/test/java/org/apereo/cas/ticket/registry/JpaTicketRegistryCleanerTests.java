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
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSchedulingConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaTicketRegistryCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    JpaTicketRegistryTicketCatalogConfiguration.class,
    JpaTicketRegistryConfiguration.class,
    CasHibernateJpaConfiguration.class,
    CasCoreTicketsSchedulingConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
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
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
})
@Transactional(transactionManager = "ticketTransactionManager", isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
@ResourceLock("jpa-tickets")
@Tag("JDBC")
public class JpaTicketRegistryCleanerTests {
    @Autowired
    @Qualifier("defaultTicketFactory")
    protected TicketFactory ticketFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("ticketRegistryCleaner")
    private TicketRegistryCleaner ticketRegistryCleaner;

    @BeforeEach
    public void cleanup() {
        ticketRegistry.deleteAll();
    }

    @Test
    public void verifyOperation() {
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);
        ticketRegistry.addTicket(tgt);

        val stFactory = (ServiceTicketFactory) ticketFactory.get(ServiceTicket.class);
        val st = stFactory.create(tgt, RegisteredServiceTestUtils.getService(), true, ServiceTicket.class);

        ticketRegistry.addTicket(st);
        ticketRegistry.updateTicket(tgt);

        assertEquals(1, ticketRegistry.sessionCount());
        assertEquals(1, ticketRegistry.serviceTicketCount());

        st.markTicketExpired();
        tgt.markTicketExpired();

        ticketRegistry.updateTicket(st);
        ticketRegistry.updateTicket(tgt);

        assertEquals(2, ticketRegistryCleaner.clean());

        assertEquals(0, ticketRegistry.sessionCount());
        assertEquals(0, ticketRegistry.serviceTicketCount());
    }

    @Test
    public void verifyTransientTicketCleaning() {
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);
        ticketRegistry.addTicket(tgt);
        
        val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val transientTicket = transientFactory.create(RegisteredServiceTestUtils.getService());
        ticketRegistry.addTicket(transientTicket);

        ticketRegistry.updateTicket(tgt);

        transientTicket.markTicketExpired();
        tgt.markTicketExpired();

        ticketRegistry.updateTicket(transientTicket);
        ticketRegistry.updateTicket(tgt);

        assertEquals(2, ticketRegistry.getTickets().size());
        assertEquals(2, ticketRegistryCleaner.clean());
        assertTrue(ticketRegistry.getTickets().isEmpty());
    }
}
