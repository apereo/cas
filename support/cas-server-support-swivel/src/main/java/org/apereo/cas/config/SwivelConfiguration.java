package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowAction;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.swivel.web.flow.rest.SwivelTuringImageGeneratorController;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.SwivelMultifactorProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SwivelConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("swivelConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SwivelConfiguration {

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
    public FlowDefinitionRegistry swivelAuthenticatorFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-swivel/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "swivelMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer swivelMultifactorWebflowConfigurer() {
        final CasWebflowConfigurer w = new SwivelMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                swivelAuthenticatorFlowRegistry(), applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver swivelAuthenticationWebflowEventResolver() {
        return new SwivelAuthenticationWebflowEventResolver(authenticationSystemSupport,
                centralAuthenticationService,
                servicesManager,
                ticketRegistrySupport,
                warnCookieGenerator,
                authenticationRequestServiceSelectionStrategies,
                multifactorAuthenticationProviderSelector);
    }

    @Bean
    public SwivelTuringImageGeneratorController swivelTuringImageGeneratorController() {
        final SwivelMultifactorProperties swivel = this.casProperties.getAuthn().getMfa().getSwivel();
        return new SwivelTuringImageGeneratorController(swivel);
    }

    @Bean
    @RefreshScope
    public Action swivelAuthenticationWebflowAction() {
        return new SwivelAuthenticationWebflowAction(swivelAuthenticationWebflowEventResolver());
    }

    /**
     * The swivel multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.swivel", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("swivelMultifactorTrustConfiguration")
    public class SwivelMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "swivelMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer swivelMultifactorTrustWebflowConfigurer() {
            final CasWebflowConfigurer w = new SwivelMultifactorTrustWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                    casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled(), 
                    swivelAuthenticatorFlowRegistry(), applicationContext, casProperties);
            w.initialize();
            return w;
        }
    }
}
