package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration(value = "casCoreTicketsSchedulingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableAsync
@EnableTransactionManagement(proxyTargetClass = true)
@AutoConfigureAfter(CasCoreTicketsConfiguration.class)
@Slf4j
public class CasCoreTicketsSchedulingConfiguration {

    @Autowired
    @Qualifier("lockingStrategy")
    private ObjectProvider<LockingStrategy> lockingStrategy;

    @Autowired
    @Qualifier(LogoutManager.DEFAULT_BEAN_NAME)
    private ObjectProvider<LogoutManager> logoutManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "ticketRegistryCleaner")
    @Bean
    @RefreshScope
    public TicketRegistryCleaner ticketRegistryCleaner() {
        val isCleanerEnabled = casProperties.getTicket().getRegistry().getCleaner().getSchedule().isEnabled();
        if (isCleanerEnabled) {
            LOGGER.debug("Ticket registry cleaner is enabled.");
            return new DefaultTicketRegistryCleaner(lockingStrategy.getObject(),
                logoutManager.getObject(), ticketRegistry.getObject());
        }
        LOGGER.debug("Ticket registry cleaner is not enabled. "
            + "Expired tickets are not forcefully cleaned by CAS. It is up to the ticket registry itself to "
            + "clean up tickets based on its own expiration and eviction policies.");
        return NoOpTicketRegistryCleaner.getInstance();
    }

    @ConditionalOnMissingBean(name = "ticketRegistryCleanerScheduler")
    @ConditionalOnProperty(prefix = "cas.ticket.registry.cleaner.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    @RefreshScope
    @Autowired
    public TicketRegistryCleanerScheduler ticketRegistryCleanerScheduler(@Qualifier("ticketRegistryCleaner")
                                                                         final TicketRegistryCleaner ticketRegistryCleaner) {
        return new TicketRegistryCleanerScheduler(ticketRegistryCleaner);
    }


    /**
     * The Ticket registry cleaner scheduler. Because the cleaner itself is marked
     * with {@link org.springframework.transaction.annotation.Transactional},
     * we need to create a separate scheduler component that invokes it
     * so that {@link Scheduled} annotations can be processed and not interfere
     * with transaction semantics of the cleaner.
     */
    @RequiredArgsConstructor
    public static class TicketRegistryCleanerScheduler {
        private final TicketRegistryCleaner ticketRegistryCleaner;

        @Scheduled(initialDelayString = "${cas.ticket.registry.cleaner.schedule.start-delay:PT30S}",
            fixedDelayString = "${cas.ticket.registry.cleaner.schedule.repeat-interval:PT120S}")
        public void run() {
            try {
                this.ticketRegistryCleaner.clean();
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
    }
}
