package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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
@Configuration(value = "CasCoreTicketsSchedulingConfiguration", proxyBeanMethods = false)
class CasCoreTicketsSchedulingConfiguration {

    @ConditionalOnMissingBean(name = TicketRegistryCleaner.BEAN_NAME)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public TicketRegistryCleaner ticketRegistryCleaner(
        final CasConfigurationProperties casProperties,
        @Qualifier(LockRepository.BEAN_NAME) final LockRepository lockRepository,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(TicketRegistry.BEAN_NAME) final TicketRegistry ticketRegistry) {
        return new DefaultTicketRegistryCleaner(lockRepository, applicationContext, ticketRegistry);
    }

    @ConditionalOnMissingBean(name = "ticketRegistryCleanerScheduler")
    @ConditionalOnMatchingHostname(name = "cas.ticket.registry.cleaner.schedule.enabled-on-host")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public Runnable ticketRegistryCleanerScheduler(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(TicketRegistryCleaner.BEAN_NAME) final TicketRegistryCleaner ticketRegistryCleaner) {
        return BeanSupplier.of(Runnable.class)
            .when(BeanCondition.on("cas.ticket.registry.cleaner.schedule.enabled").isTrue()
                .evenIfMissing().given(applicationContext.getEnvironment()))
            .supply(() -> {
                LOGGER.debug("Ticket registry cleaner is enabled to run on schedule.");
                return new TicketRegistryCleanerScheduler(ticketRegistryCleaner);
            })
            .otherwiseProxy(__ -> LOGGER.info("Ticket registry cleaner is not enabled to run on schedule. "
                + "Expired tickets are not forcefully cleaned by CAS. It is up to the ticket registry itself to "
                + "clean up tickets based on its own expiration and eviction policies."))
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
    static class TicketRegistryCleanerScheduler implements Runnable {
        private final TicketRegistryCleaner ticketRegistryCleaner;

        @Scheduled(
            cron = "${cas.ticket.registry.cleaner.schedule.cron-expression:}",
            zone = "${cas.ticket.registry.cleaner.schedule.cron-time-zone:}",
            initialDelayString = "${cas.ticket.registry.cleaner.schedule.start-delay:PT30S}",
            fixedDelayString = "${cas.ticket.registry.cleaner.schedule.repeat-interval:PT120S}")
        @Override
        public void run() {
            FunctionUtils.doAndHandle(__ -> ticketRegistryCleaner.clean());
        }
    }
}
