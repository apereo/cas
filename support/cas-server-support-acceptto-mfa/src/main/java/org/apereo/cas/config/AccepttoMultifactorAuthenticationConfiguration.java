package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.keys.RsaKeyUtil;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.security.PublicKey;

/**
 * This is {@link AccepttoMultifactorAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableRetry
@Slf4j
@Configuration(value = "accepttoMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
public class AccepttoMultifactorAuthenticationConfiguration {

    @Configuration(value = "AccepttoMultifactorAuthenticationCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationCoreConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mfaAccepttoApiPublicKey")
        @Autowired
        public PublicKey mfaAccepttoApiPublicKey(final CasConfigurationProperties casProperties) throws Exception {
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

        @ConditionalOnMissingBean(name = "casAccepttoQRCodePrincipalFactory")
        @Bean
        public PrincipalFactory casAccepttoQRCodePrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }
    }

    @Configuration(value = "AccepttoMultifactorAuthenticationWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationWebflowConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "mfaAccepttoAuthenticatorFlowRegistry")
        @Autowired
        public FlowDefinitionRegistry mfaAccepttoAuthenticatorFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, AccepttoMultifactorWebflowConfigurer.MFA_ACCEPTTO_EVENT_ID);
            return builder.build();
        }

        @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer mfaAccepttoMultifactorWebflowConfigurer(
            final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
            @Qualifier("mfaAccepttoAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry mfaAccepttoAuthenticatorFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new AccepttoMultifactorWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, mfaAccepttoAuthenticatorFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        }


        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowEventResolver mfaAccepttoMultifactorAuthenticationWebflowEventResolver(
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new AccepttoMultifactorAuthenticationWebflowEventResolver(casWebflowConfigurationContext);
        }

    }

    @Configuration(value = "AccepttoMultifactorAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationWebflowPlanConfiguration {
        @ConditionalOnMissingBean(name = "mfaAccepttoCasWebflowExecutionPlanConfigurer")
        @Bean
        public CasWebflowExecutionPlanConfigurer mfaAccepttoCasWebflowExecutionPlanConfigurer(
            @Qualifier("mfaAccepttoMultifactorWebflowConfigurer")
            final CasWebflowConfigurer mfaAccepttoMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(mfaAccepttoMultifactorWebflowConfigurer);
        }

    }

    @Configuration(value = "AccepttoMultifactorAuthenticationSessionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationSessionConfiguration {

        @ConditionalOnMissingBean(name = "mfaAccepttoDistributedSessionStore")
        @Bean
        @Autowired
        public SessionStore mfaAccepttoDistributedSessionStore(
            final CasConfigurationProperties casProperties,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("defaultTicketFactory")
            final TicketFactory ticketFactory) {
            val cookie = casProperties.getSessionReplication().getCookie();
            val cookieGenerator = CookieUtils.buildCookieRetrievingGenerator(cookie);
            return new DistributedJEESessionStore(centralAuthenticationService, ticketFactory, cookieGenerator);
        }

    }

    @Configuration(value = "AccepttoMultifactorAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationPlanConfiguration {

        @ConditionalOnMissingBean(name = "casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer")
        @Bean
        public AuthenticationEventExecutionPlanConfigurer casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer(
            @Qualifier("casAccepttoQRCodeAuthenticationHandler")
            final AuthenticationHandler casAccepttoQRCodeAuthenticationHandler,
            @Qualifier("casAccepttoQRCodeAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator casAccepttoQRCodeAuthenticationMetaDataPopulator,
            @Qualifier("defaultPrincipalResolver")
            final PrincipalResolver defaultPrincipalResolver) {
            return plan -> {
                plan.registerAuthenticationHandlerWithPrincipalResolver(casAccepttoQRCodeAuthenticationHandler, defaultPrincipalResolver);
                plan.registerAuthenticationMetadataPopulator(casAccepttoQRCodeAuthenticationMetaDataPopulator);
                plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(AccepttoEmailCredential.class));
            };
        }

    }

    @Configuration(value = "AccepttoMultifactorAuthenticationHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationHandlerConfiguration {
        @ConditionalOnMissingBean(name = "casAccepttoQRCodeAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandler casAccepttoQRCodeAuthenticationHandler(
            @Qualifier("casAccepttoQRCodePrincipalFactory")
            final PrincipalFactory casAccepttoQRCodePrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new AccepttoQRCodeAuthenticationHandler(servicesManager, casAccepttoQRCodePrincipalFactory);
        }
    }
    
    @Configuration(value = "AccepttoMultifactorAuthenticationMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationMetadataConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationMetaDataPopulator casAccepttoQRCodeAuthenticationMetaDataPopulator(
            final CasConfigurationProperties casProperties,
            @Qualifier("casAccepttoQRCodeAuthenticationHandler")
            final AuthenticationHandler casAccepttoQRCodeAuthenticationHandler,
            @Qualifier("casAccepttoMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider casAccepttoMultifactorAuthenticationProvider) {
            return new AuthenticationContextAttributeMetaDataPopulator(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
                casAccepttoQRCodeAuthenticationHandler,
                casAccepttoMultifactorAuthenticationProvider.getId());
        }

    }
    @Configuration(value = "AccepttoMultifactorAuthenticationActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationActionsConfiguration {

        @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorValidateUserDeviceRegistrationAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action mfaAccepttoMultifactorValidateUserDeviceRegistrationAction(final CasConfigurationProperties casProperties) {
            return new AccepttoMultifactorValidateUserDeviceRegistrationAction(casProperties);
        }

        @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorFetchChannelAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action mfaAccepttoMultifactorFetchChannelAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaAccepttoDistributedSessionStore")
            final SessionStore mfaAccepttoDistributedSessionStore,
            @Qualifier("mfaAccepttoApiPublicKey")
            final PublicKey mfaAccepttoApiPublicKey) throws Exception {
            return new AccepttoMultifactorFetchChannelAction(casProperties, mfaAccepttoDistributedSessionStore, mfaAccepttoApiPublicKey);
        }

        @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorValidateChannelAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action mfaAccepttoMultifactorValidateChannelAction(
            @Qualifier("mfaAccepttoDistributedSessionStore")
            final SessionStore mfaAccepttoDistributedSessionStore,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport) {
            return new AccepttoMultifactorValidateChannelAction(mfaAccepttoDistributedSessionStore, authenticationSystemSupport);
        }

        @Bean
        @ConditionalOnMissingBean(name = "mfaAccepttoQRCodeValidateWebSocketChannelAction")
        @Autowired
        public Action mfaAccepttoQRCodeValidateWebSocketChannelAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaAccepttoDistributedSessionStore")
            final SessionStore mfaAccepttoDistributedSessionStore) {
            return new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, mfaAccepttoDistributedSessionStore);
        }


        @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorFinalizeAuthenticationWebflowAction")
        @Bean
        public Action mfaAccepttoMultifactorFinalizeAuthenticationWebflowAction(
            @Qualifier("mfaAccepttoMultifactorAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver mfaAccepttoMultifactorAuthenticationWebflowEventResolver) {
            return new AccepttoMultifactorFinalizeAuthenticationWebflowAction(mfaAccepttoMultifactorAuthenticationWebflowEventResolver);
        }


        @ConditionalOnMissingBean(name = "mfaAccepttoMultifactorDetermineUserAccountStatusAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action mfaAccepttoMultifactorDetermineUserAccountStatusAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaAccepttoApiPublicKey")
            final PublicKey mfaAccepttoApiPublicKey) throws Exception {
            return new AccepttoMultifactorDetermineUserAccountStatusAction(casProperties, mfaAccepttoApiPublicKey);
        }


    }
}
