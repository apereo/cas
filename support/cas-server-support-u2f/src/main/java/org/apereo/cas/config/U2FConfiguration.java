package org.apereo.cas.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FInMemoryDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FJsonResourceDeviceRepository;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.u2f.web.flow.U2FMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartAuthenticationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartRegistrationAction;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link U2FConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("u2fConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FConfiguration.class);

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired(required = false)
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector =
            new RankedMultifactorAuthenticationProviderSelector();

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Bean
    public FlowDefinitionRegistry u2fFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-u2f/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "u2fAuthenticationWebflowAction")
    @Bean
    public Action u2fAuthenticationWebflowAction() {
        return new U2FAuthenticationWebflowAction(u2fAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "u2fMultifactorWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer u2fMultifactorWebflowConfigurer() {
        final CasWebflowConfigurer w = new U2FMultifactorWebflowConfigurer(flowBuilderServices, 
                loginFlowDefinitionRegistry, u2fFlowRegistry(), applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @ConditionalOnMissingBean(name = "u2fStartAuthenticationAction")
    @Bean
    public Action u2fStartAuthenticationAction() {
        return new U2FStartAuthenticationAction(casProperties.getServer().getName(), u2fDeviceRepository());
    }

    @ConditionalOnMissingBean(name = "u2fStartRegistrationAction")
    @Bean
    public Action u2fStartRegistrationAction() {
        return new U2FStartRegistrationAction(casProperties.getServer().getName(), u2fDeviceRepository());
    }

    @ConditionalOnMissingBean(name = "u2fCheckAccountRegistrationAction")
    @Bean
    public Action u2fCheckAccountRegistrationAction() {
        return new U2FAccountCheckRegistrationAction(u2fDeviceRepository());
    }

    @ConditionalOnMissingBean(name = "u2fSaveAccountRegistrationAction")
    @Bean
    public Action u2fSaveAccountRegistrationAction() {
        return new U2FAccountSaveRegistrationAction(u2fDeviceRepository());
    }

    @ConditionalOnMissingBean(name = "u2fAuthenticationWebflowEventResolver")
    @Bean
    public CasWebflowEventResolver u2fAuthenticationWebflowEventResolver() {
        return new U2FAuthenticationWebflowEventResolver(authenticationSystemSupport, centralAuthenticationService,
                servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies,
                multifactorAuthenticationProviderSelector);
    }

    @ConditionalOnMissingBean(name = "transactionManagerU2f")
    @Bean
    public PlatformTransactionManager transactionManagerU2f() {
        return new PseudoPlatformTransactionManager();
    }

    @ConditionalOnMissingBean(name = "u2fDeviceRepositoryCleanerScheduler")
    @Bean
    @Autowired
    @ConditionalOnProperty(prefix = "authn.mfa.u2f.cleaner", name = "enabled", havingValue = "true", matchIfMissing = true)
    public U2FDeviceRepositoryCleanerScheduler u2fDeviceRepositoryCleanerScheduler(
            @Qualifier("u2fDeviceRepository") final U2FDeviceRepository storage) {
        return new U2FDeviceRepositoryCleanerScheduler(storage);
    }

    @ConditionalOnMissingBean(name = "u2fDeviceRepository")
    @Bean
    public U2FDeviceRepository u2fDeviceRepository() {
        final U2FMultifactorProperties u2f = casProperties.getAuthn().getMfa().getU2f();

        final LoadingCache<String, String> requestStorage =
                Caffeine.newBuilder()
                        .expireAfterWrite(u2f.getExpireRegistrations(), u2f.getExpireRegistrationsTimeUnit())
                        .build(key -> StringUtils.EMPTY);

        if (u2f.getJson().getLocation() != null) {
            return new U2FJsonResourceDeviceRepository(requestStorage,
                    u2f.getJson().getLocation(),
                    u2f.getExpireRegistrations(), u2f.getExpireDevicesTimeUnit());
        }

        final LoadingCache<String, Map<String, String>> userStorage =
                Caffeine.newBuilder()
                        .expireAfterWrite(u2f.getExpireDevices(), u2f.getExpireDevicesTimeUnit())
                        .build(key -> new HashMap<>());
        return new U2FInMemoryDeviceRepository(userStorage, requestStorage);
    }

    /**
     * The device cleaner scheduler.
     */
    public static class U2FDeviceRepositoryCleanerScheduler {
        private final U2FDeviceRepository repository;

        public U2FDeviceRepositoryCleanerScheduler(final U2FDeviceRepository repository) {
            this.repository = repository;
        }

        @Scheduled(initialDelayString = "${cas.authn.mfa.u2f.cleaner.schedule.startDelay:PT20S}",
                fixedDelayString = "${cas.authn.mfa.u2f.cleaner.schedule.repeatInterval:PT15M}")
        public void run() {
            LOGGER.debug("Starting to clean expired U2F devices from repository");
            this.repository.clean();
        }
    }
}
