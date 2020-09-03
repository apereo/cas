package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
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
import org.apereo.cas.integration.pac4j.DistributedJEESessionStore;
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
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.keys.RsaKeyUtil;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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

import java.security.PublicKey;

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
    @Qualifier("authenticationEventExecutionPlan")
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

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
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    @Qualifier("casAccepttoMultifactorAuthenticationProvider")
    private ObjectProvider<MultifactorAuthenticationProvider> casAccepttoMultifactorAuthenticationProvider;

    @Bean
    public FlowDefinitionRegistry mfaAccepttoAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices.getObject());
        builder.setBasePath(CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/mfa-acceptto/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaAccepttoMultifactorWebflowConfigurer() {
        return new AccepttoMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            mfaAccepttoAuthenticatorFlowRegistry(), applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoCasWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer mfaAccepttoCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(mfaAccepttoMultifactorWebflowConfigurer());
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoDistributedSessionStore")
    @Bean
    public SessionStore<JEEContext> mfaAccepttoDistributedSessionStore() {
        val cookie = casProperties.getSessionReplication().getCookie();
        val cookieGenerator = CookieUtils.buildCookieRetrievingGenerator(cookie);
        return new DistributedJEESessionStore(centralAuthenticationService.getObject(), ticketFactory.getObject(), cookieGenerator);
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorFetchChannelAction")
    @Bean
    @RefreshScope
    public Action mfaAccepttoMultifactorFetchChannelAction() throws Exception {
        return new AccepttoMultifactorFetchChannelAction(casProperties, mfaAccepttoDistributedSessionStore(), mfaAccepttoApiPublicKey());
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorValidateChannelAction")
    @Bean
    @RefreshScope
    public Action mfaAccepttoMultifactorValidateChannelAction() {
        return new AccepttoMultifactorValidateChannelAction(mfaAccepttoDistributedSessionStore(),
            authenticationSystemSupport.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "mfaAccepttoQRCodeValidateWebSocketChannelAction")
    public Action mfaAccepttoQRCodeValidateWebSocketChannelAction() {
        return new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore());
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorDetermineUserAccountStatusAction")
    @Bean
    @RefreshScope
    public Action mfaAccepttoMultifactorDetermineUserAccountStatusAction() throws Exception {
        return new AccepttoMultifactorDetermineUserAccountStatusAction(casProperties, mfaAccepttoApiPublicKey());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mfaAccepttoApiPublicKey")
    public PublicKey mfaAccepttoApiPublicKey() throws Exception {
        val props = casProperties.getAuthn().getMfa().getAcceptto();
        val location = props.getRegistrationApiPublicKey().getLocation();
        if (location == null) {
            throw new BeanCreationException("No registration API public key is defined for the Acceptto integration.");
        }
        val factory = new PublicKeyFactoryBean(location, RsaKeyUtil.RSA);
        LOGGER.debug("Locating Acceptto registration API public key from [{}]", location);
        factory.setSingleton(false);
        return factory.getObject();
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorValidateUserDeviceRegistrationAction")
    @Bean
    @RefreshScope
    public Action mfaAccepttoMultifactorValidateUserDeviceRegistrationAction() {
        return new AccepttoMultifactorValidateUserDeviceRegistrationAction(casProperties);
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver mfaAccepttoMultifactorAuthenticationWebflowEventResolver() {
        val context = CasWebflowEventResolutionConfigurationContext.builder()
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .servicesManager(servicesManager.getObject())
            .ticketRegistrySupport(ticketRegistrySupport.getObject())
            .warnCookieGenerator(warnCookieGenerator.getObject())
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies.getObject())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .casProperties(casProperties)
            .ticketRegistry(ticketRegistry.getObject())
            .applicationContext(applicationContext)
            .authenticationEventExecutionPlan(authenticationEventExecutionPlan.getObject())
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
            servicesManager.getObject(),
            casAccepttoQRCodePrincipalFactory());
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator casAccepttoQRCodeAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            casAccepttoQRCodeAuthenticationHandler(),
            casAccepttoMultifactorAuthenticationProvider.getObject().getId()
        );
    }

    @ConditionalOnMissingBean(name = "casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandlerWithPrincipalResolver(casAccepttoQRCodeAuthenticationHandler(), defaultPrincipalResolver.getObject());
            plan.registerAuthenticationMetadataPopulator(casAccepttoQRCodeAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(AccepttoEmailCredential.class));
        };
    }

}
