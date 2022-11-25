package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.lock.LockRepository;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMatchingHostname;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.Cleanable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is {@link CasCoreTicketsSchedulingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableAsync(proxyTargetClass = false)
@EnableTransactionManagement(proxyTargetClass = false)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry)
@AutoConfiguration(after = CasCoreTicketsConfiguration.class)
public class CasCoreTicketsSchedulingConfiguration {

    @ConditionalOnMissingBean(name = "ticketRegistryCleaner")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistryCleaner ticketRegistryCleaner(
        final CasConfigurationProperties casProperties,
        @Qualifier(LockRepository.BEAN_NAME) final LockRepository lockRepository,
        @Qualifier(LogoutManager.DEFAULT_BEAN_NAME) final LogoutManager logoutManager,
        @Qualifier(TicketRegistry.BEAN_NAME) final TicketRegistry ticketRegistry) {
        val isCleanerEnabled = casProperties.getTicket().getRegistry().getCleaner().getSchedule().isEnabled();
        if (isCleanerEnabled) {
            LOGGER.debug("Ticket registry cleaner is enabled.");
            return new DefaultTicketRegistryCleaner(lockRepository, logoutManager, ticketRegistry);
        }
        LOGGER.debug("Ticket registry cleaner is not enabled. "
                     + "Expired tickets are not forcefully cleaned by CAS. It is up to the ticket registry itself to "
                     + "clean up tickets based on its own expiration and eviction policies.");
        return NoOpTicketRegistryCleaner.getInstance();
    }

    @ConditionalOnMissingBean(name = "ticketRegistryCleanerScheduler")
    @ConditionalOnMatchingHostname(name = "cas.ticket.registry.cleaner.schedule.enabled-on-host")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Cleanable ticketRegistryCleanerScheduler(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("ticketRegistryCleaner") final TicketRegistryCleaner ticketRegistryCleaner) throws Exception {
        return BeanSupplier.of(Cleanable.class)
            .when(BeanCondition.on("cas.ticket.registry.cleaner.schedule.enabled").isTrue()
                .evenIfMissing().given(applicationContext.getEnvironment()))
            .supply(() -> new TicketRegistryCleanerScheduler(ticketRegistryCleaner))
            .otherwiseProxy()
            .get();
    }


    /**
     * The Ticket registry cleaner scheduler. Because the cleaner itself is marked
     * with {@link Transactional},
     * we need to create a separate scheduler component that invokes it
     * so that {@link Scheduled} annotations can be processed and not interfere
     * with transaction semantics of the cleaner.
     */
    @RequiredArgsConstructor
    public static class TicketRegistryCleanerScheduler implements Cleanable {
        private final TicketRegistryCleaner ticketRegistryCleaner;

        @Scheduled(initialDelayString = "${cas.ticket.registry.cleaner.schedule.start-delay:PT30S}",
            fixedDelayString = "${cas.ticket.registry.cleaner.schedule.repeat-interval:PT120S}")
        @Override
        public void clean() {
            FunctionUtils.doAndHandle(__ -> ticketRegistryCleaner.clean());
        }
    }
}
