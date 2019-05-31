package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.integration.pac4j.DistributedJ2ESessionStore;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorDetermineUserAccountStatusAction;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorFetchChannelAction;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorFinalizeAuthenticationWebflowAction;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorValidateChannelAction;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorValidateUserDeviceRegistrationAction;
import org.apereo.cas.mfa.accepto.web.flow.AccepttoMultifactorWebflowConfigurer;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeAuthenticationHandler;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeValidateWebSocketChannelAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link AccepttoMultifactorAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("accepttoMultifactorAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableRetry
@Slf4j
public class AccepttoMultifactorAuthenticationConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    @Qualifier("casAccepttoMultifactorAuthenticationProvider")
    private ObjectProvider<MultifactorAuthenticationProvider> casAccepttoMultifactorAuthenticationProvider;

    @Bean
    public FlowDefinitionRegistry mfaAccepttoAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath(CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/mfa-acceptto/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaAccepttoMultifactorWebflowConfigurer() {
        return new AccepttoMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry.getIfAvailable(),
            mfaAccepttoAuthenticatorFlowRegistry(), applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoCasWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer mfaAccepttoCasWebflowExecutionPlanConfigurer() {
        return new CasWebflowExecutionPlanConfigurer() {
            @Override
            public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
                plan.registerWebflowConfigurer(mfaAccepttoMultifactorWebflowConfigurer());
            }
        };
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoDistributedSessionStore")
    @Bean
    public SessionStore<J2EContext> mfaAccepttoDistributedSessionStore() {
        return new DistributedJ2ESessionStore(ticketRegistry.getIfAvailable(), ticketFactory.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorFetchChannelAction")
    @Bean
    public Action mfaAccepttoMultifactorFetchChannelAction() {
        return new AccepttoMultifactorFetchChannelAction(casProperties, mfaAccepttoDistributedSessionStore());
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorValidateChannelAction")
    @Bean
    public Action mfaAccepttoMultifactorValidateChannelAction() {
        return new AccepttoMultifactorValidateChannelAction(mfaAccepttoDistributedSessionStore(),
            authenticationSystemSupport.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "mfaAccepttoQRCodeValidateWebSocketChannelAction")
    public Action mfaAccepttoQRCodeValidateWebSocketChannelAction() {
        return new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore());
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorDetermineUserAccountStatusAction")
    @Bean
    public Action mfaAccepttoMultifactorDetermineUserAccountStatusAction() throws Exception {
        val props = casProperties.getAuthn().getMfa().getAcceptto();
        val location = props.getRegistrationApiPublicKey().getLocation();
        if (location == null) {
            throw new BeanCreationException("No registration API public key is defined for the Acceptto integration.");
        }
        val factory = new PublicKeyFactoryBean();
        LOGGER.debug("Locating Acceptto registration API public key from [{}]", location);
        factory.setResource(location);
        factory.setSingleton(false);
        factory.setAlgorithm("RSA");
        return new AccepttoMultifactorDetermineUserAccountStatusAction(casProperties, factory.getObject());
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorValidateUserDeviceRegistrationAction")
    @Bean
    public Action mfaAccepttoMultifactorValidateUserDeviceRegistrationAction() {
        return new AccepttoMultifactorValidateUserDeviceRegistrationAction(casProperties, authenticationSystemSupport.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver mfaAccepttoMultifactorAuthenticationWebflowEventResolver() {
        val context = CasWebflowEventResolutionConfigurationContext.builder()
            .authenticationSystemSupport(authenticationSystemSupport.getIfAvailable())
            .centralAuthenticationService(centralAuthenticationService.getIfAvailable())
            .servicesManager(servicesManager.getIfAvailable())
            .ticketRegistrySupport(ticketRegistrySupport.getIfAvailable())
            .warnCookieGenerator(warnCookieGenerator.getIfAvailable())
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies.getIfAvailable())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getIfAvailable())
            .casProperties(casProperties)
            .eventPublisher(applicationEventPublisher)
            .applicationContext(applicationContext)
            .build();

        return new AccepttoMultifactorAuthenticationWebflowEventResolver(context);
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorFinalizeAuthenticationWebflowAction")
    @Bean
    public Action mfaAccepttoMultifactorFinalizeAuthenticationWebflowAction() {
        return new AccepttoMultifactorFinalizeAuthenticationWebflowAction(mfaAccepttoMultifactorAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "casAccepttoQRCodePrincipalFactory")
    @Bean
    public PrincipalFactory casAccepttoQRCodePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "casAccepttoQRCodeAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler casAccepttoQRCodeAuthenticationHandler() {
        val props = casProperties.getAuthn().getMfa().getAcceptto();
        if (StringUtils.isBlank(props.getApiUrl()) || StringUtils.isBlank(props.getApplicationId())
            || StringUtils.isBlank(props.getSecret())) {
            throw new BeanCreationException("No API url, application id or secret "
                + "is defined for the Acceptto integration. Examine your CAS configuration and adjust for correct values.");
        }
        return new AccepttoQRCodeAuthenticationHandler(
            servicesManager.getIfAvailable(),
            casAccepttoQRCodePrincipalFactory());
    }

    @Bean
    public AuthenticationMetaDataPopulator casAccepttoQRCodeAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            casAccepttoQRCodeAuthenticationHandler(),
            casAccepttoMultifactorAuthenticationProvider.getIfAvailable().getId()
        );
    }

    @ConditionalOnMissingBean(name = "casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandlerWithPrincipalResolver(casAccepttoQRCodeAuthenticationHandler(), defaultPrincipalResolver.getIfAvailable());
            plan.registerAuthenticationMetadataPopulator(casAccepttoQRCodeAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(AccepttoEmailCredential.class));
        };
    }

}
