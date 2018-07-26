package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link CasCoreTicketsSchedulingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreTicketsSchedulingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableAsync
@EnableTransactionManagement(proxyTargetClass = true)
@AutoConfigureAfter(CasCoreTicketsConfiguration.class)
public class CasCoreTicketsSchedulingConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreTicketsSchedulingConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "ticketRegistryCleaner")
    @Bean
    @Autowired
    public TicketRegistryCleaner ticketRegistryCleaner(@Qualifier("lockingStrategy") final LockingStrategy lockingStrategy,
                                                       @Qualifier("logoutManager") final LogoutManager logoutManager,
                                                       @Qualifier("ticketRegistry") final TicketRegistry ticketRegistry) {
        final boolean isCleanerEnabled = casProperties.getTicket().getRegistry().getCleaner().getSchedule().isEnabled();
        if (isCleanerEnabled) {
            LOGGER.debug("Ticket registry cleaner is enabled.");
            return new DefaultTicketRegistryCleaner(lockingStrategy, logoutManager, ticketRegistry);
        }
        LOGGER.debug("Ticket registry cleaner is not enabled. "
                + "Expired tickets are not forcefully collected and cleaned by CAS. It is up to the ticket registry itself to "
                + "clean up tickets based on expiration and eviction policies.");
        return NoOpTicketRegistryCleaner.getInstance();
    }

    @ConditionalOnMissingBean(name = "ticketRegistryCleanerScheduler")
    @ConditionalOnProperty(prefix = "cas.ticket.registry.cleaner", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    @Autowired
    @RefreshScope
    public TicketRegistryCleanerScheduler ticketRegistryCleanerScheduler(@Qualifier("ticketRegistryCleaner") 
                                                                         final TicketRegistryCleaner ticketRegistryCleaner) {
        return new TicketRegistryCleanerScheduler(ticketRegistryCleaner);
    }


    /**
     * The Ticket registry cleaner scheduler. Because the cleaner itself is marked
     * with {@link org.springframework.transaction.annotation.Transactional},
     * we need to create a separate scheduler component that simply invokes it
     * so that {@link Scheduled} annotations can be processed and not interfere
     * with transaction semantics of the cleaner.
     */
    public static class TicketRegistryCleanerScheduler {
        private final TicketRegistryCleaner ticketRegistryCleaner;

        public TicketRegistryCleanerScheduler(final TicketRegistryCleaner ticketRegistryCleaner) {
            this.ticketRegistryCleaner = ticketRegistryCleaner;
        }

        @Scheduled(initialDelayString = "${cas.ticket.registry.cleaner.schedule.startDelay:PT30S}",
                fixedDelayString = "${cas.ticket.registry.cleaner.schedule.repeatInterval:PT120S}")
        public void run() {
            try {
                this.ticketRegistryCleaner.clean();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
