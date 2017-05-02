package org.apereo.cas.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FInMemoryDeviceRepository;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.u2f.web.flow.U2FMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartAuthenticationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartRegistrationAction;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        return new U2FMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, u2fFlowRegistry());
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

    @ConditionalOnMissingBean(name = "u2fDeviceRepository")
    @Bean
    public U2FDeviceRepository u2fDeviceRepository() {
        final MultifactorAuthenticationProperties.U2F u2f = casProperties.getAuthn().getMfa().getU2f();

        final LoadingCache<String, Map<String, String>> userStorage =
                CacheBuilder.newBuilder()
                        .expireAfterWrite(u2f.getMemory().getExpireDevices(), u2f.getMemory().getExpireDevicesTimeUnit())
                        .build(new CacheLoader<String, Map<String, String>>() {
                            @Override
                            public Map<String, String> load(final String key) throws Exception {
                                return new HashMap<>();
                            }
                        });

        final LoadingCache<String, String> requestStorage =
                CacheBuilder.newBuilder()
                        .expireAfterWrite(u2f.getMemory().getExpireRegistrations(), u2f.getMemory().getExpireRegistrationsTimeUnit())
                        .build(new CacheLoader<String, String>() {
                            @Override
                            public String load(final String key) throws Exception {
                                return StringUtils.EMPTY;
                            }
                        });

        return new U2FInMemoryDeviceRepository(userStorage, requestStorage);
    }
}
