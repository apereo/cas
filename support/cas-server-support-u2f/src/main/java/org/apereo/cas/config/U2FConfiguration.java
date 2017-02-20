package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistrationRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FInMemoryDeviceRegistrationRepository;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.u2f.web.flow.U2FMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartAuthenticationAction;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.List;

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
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

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
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector = new FirstMultifactorAuthenticationProviderSelector();

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

    @RefreshScope
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
        return new U2FStartAuthenticationAction(casProperties.getServer().getName());
    }

    @ConditionalOnMissingBean(name = "u2fStartRegistrationAction")
    @Bean
    public Action u2fStartRegistrationAction() {
        return new U2FStartRegistrationAction(casProperties.getServer().getName(), deviceRegistrationRepository());
    }

    @ConditionalOnMissingBean(name = "u2fCheckAccountRegistrationAction")
    @Bean
    public Action u2fCheckAccountRegistrationAction() {
        return new U2FAccountCheckRegistrationAction(deviceRegistrationRepository());
    }

    @ConditionalOnMissingBean(name = "u2fSaveAccountRegistrationAction")
    @Bean
    public Action u2fSaveAccountRegistrationAction() {
        return new U2FAccountSaveRegistrationAction(deviceRegistrationRepository());
    }
    
    @ConditionalOnMissingBean(name = "u2fAuthenticationWebflowEventResolver")
    @Bean
    public CasWebflowEventResolver u2fAuthenticationWebflowEventResolver() {
        return new U2FAuthenticationWebflowEventResolver(authenticationSystemSupport, centralAuthenticationService,
                servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies,
                multifactorAuthenticationProviderSelector);
    }

    @ConditionalOnMissingBean(name = "deviceRegistrationRepository")
    @Bean
    public U2FDeviceRegistrationRepository deviceRegistrationRepository() {
        return new U2FInMemoryDeviceRegistrationRepository();
    }
}
