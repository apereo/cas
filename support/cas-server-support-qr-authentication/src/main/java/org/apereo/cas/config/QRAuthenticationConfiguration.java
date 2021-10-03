package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.qr.QRAuthenticationConstants;
import org.apereo.cas.qr.authentication.JsonResourceQRAuthenticationDeviceRepository;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.qr.authentication.QRAuthenticationTokenAuthenticationHandler;
import org.apereo.cas.qr.authentication.QRAuthenticationTokenCredential;
import org.apereo.cas.qr.validation.DefaultQRAuthenticationTokenValidatorService;
import org.apereo.cas.qr.validation.QRAuthenticationTokenValidatorService;
import org.apereo.cas.qr.web.QRAuthenticationChannelController;
import org.apereo.cas.qr.web.QRAuthenticationDeviceRepositoryEndpoint;
import org.apereo.cas.qr.web.flow.QRAuthenticationGenerateCodeAction;
import org.apereo.cas.qr.web.flow.QRAuthenticationValidateTokenAction;
import org.apereo.cas.qr.web.flow.QRAuthenticationWebflowConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link QRAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "QRAuthenticationConfiguration", proxyBeanMethods = false)
public class QRAuthenticationConfiguration {


    @Configuration(value = "QRAuthenticationServiceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationServiceConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "qrAuthenticationTokenValidatorService")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public QRAuthenticationTokenValidatorService qrAuthenticationTokenValidatorService(
            final CasConfigurationProperties casProperties,
            @Qualifier("qrAuthenticationDeviceRepository")
            final QRAuthenticationDeviceRepository qrAuthenticationDeviceRepository,
            @Qualifier("tokenTicketJwtBuilder")
            final JwtBuilder tokenTicketJwtBuilder,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new DefaultQRAuthenticationTokenValidatorService(tokenTicketJwtBuilder,
                centralAuthenticationService, casProperties, qrAuthenticationDeviceRepository);
        }

        @Bean
        @ConditionalOnMissingBean(name = "qrAuthenticationDeviceRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public QRAuthenticationDeviceRepository qrAuthenticationDeviceRepository(final CasConfigurationProperties casProperties) {
            val qr = casProperties.getAuthn().getQr();
            if (qr.getJson().getLocation() != null) {
                return new JsonResourceQRAuthenticationDeviceRepository(qr.getJson().getLocation());
            }
            return QRAuthenticationDeviceRepository.permitAll();
        }
    }

    @Configuration(value = "QRAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationWebflowPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "qrAuthenticationCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer qrAuthenticationCasWebflowExecutionPlanConfigurer(
            @Qualifier("qrAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer qrAuthenticationWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(qrAuthenticationWebflowConfigurer);
        }

    }

    @Configuration(value = "QRAuthenticationWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationWebflowConfiguration {
        @ConditionalOnMissingBean(name = "qrAuthenticationWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer qrAuthenticationWebflowConfigurer(
            final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new QRAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        }

    }

    @Configuration(value = "QRAuthenticationHandlerPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationHandlerPlanConfiguration {
        @ConditionalOnMissingBean(name = "qrAuthenticationEventExecutionPlanConfigurer")
        @Bean
        public AuthenticationEventExecutionPlanConfigurer qrAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("qrAuthenticationTokenAuthenticationHandler")
            final AuthenticationHandler qrAuthenticationTokenAuthenticationHandler) {
            return plan -> {
                plan.registerAuthenticationHandler(qrAuthenticationTokenAuthenticationHandler);
                plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(QRAuthenticationTokenCredential.class));
            };
        }

    }

    @Configuration(value = "QRAuthenticationHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationHandlerConfiguration {
        @ConditionalOnMissingBean(name = "qrAuthenticationPrincipalFactory")
        @Bean
        public PrincipalFactory qrAuthenticationPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @ConditionalOnMissingBean(name = "qrAuthenticationTokenAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandler qrAuthenticationTokenAuthenticationHandler(
            @Qualifier("qrAuthenticationPrincipalFactory")
            final PrincipalFactory qrAuthenticationPrincipalFactory,
            @Qualifier("qrAuthenticationTokenValidatorService")
            final QRAuthenticationTokenValidatorService qrAuthenticationTokenValidatorService,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new QRAuthenticationTokenAuthenticationHandler(servicesManager, qrAuthenticationPrincipalFactory, qrAuthenticationTokenValidatorService);
        }

    }

    @Configuration(value = "QRAuthenticationWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationWebConfiguration {

        @Bean
        @ConditionalOnAvailableEndpoint
        @Autowired
        public QRAuthenticationDeviceRepositoryEndpoint qrAuthenticationDeviceRepositoryEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("qrAuthenticationDeviceRepository")
            final QRAuthenticationDeviceRepository qrAuthenticationDeviceRepository) {
            return new QRAuthenticationDeviceRepositoryEndpoint(casProperties, qrAuthenticationDeviceRepository);
        }
    }

    @Configuration(value = "QRAuthenticationMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationMvcConfiguration {
        @Bean
        public WebSocketMessageBrokerConfigurer qrAuthenticationWebSocketMessageBrokerConfigurer(
            final CasConfigurationProperties casProperties) {
            return new WebSocketMessageBrokerConfigurer() {
                @Override
                public void registerStompEndpoints(final StompEndpointRegistry registry) {
                    registry.addEndpoint("/qr-websocket")
                        .setAllowedOrigins(casProperties.getAuthn().getQr().getAllowedOrigins().toArray(ArrayUtils.EMPTY_STRING_ARRAY))
                        .addInterceptors(new HttpSessionHandshakeInterceptor())
                        .withSockJS();
                }

                @Override
                public void configureMessageBroker(final MessageBrokerRegistry config) {
                    config.enableSimpleBroker(QRAuthenticationConstants.QR_SIMPLE_BROKER_DESTINATION_PREFIX);
                    config.setApplicationDestinationPrefixes("/qr");
                }
            };
        }
    }

    @Configuration(value = "QRAuthenticationControllerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationControllerConfiguration {
        @Autowired
        @Bean
        public QRAuthenticationChannelController qrAuthenticationChannelController(
            @Qualifier("brokerMessagingTemplate")
            final SimpMessagingTemplate template,
            @Qualifier("qrAuthenticationTokenValidatorService")
            final QRAuthenticationTokenValidatorService qrAuthenticationTokenValidatorService) {
            return new QRAuthenticationChannelController(template, qrAuthenticationTokenValidatorService);
        }

    }

    @Configuration(value = "QRAuthenticationWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class QRAuthenticationWebflowActionConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "qrAuthenticationValidateWebSocketChannelAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action qrAuthenticationValidateWebSocketChannelAction() {
            return new QRAuthenticationValidateTokenAction();
        }

        @Bean
        @ConditionalOnMissingBean(name = "qrAuthenticationGenerateCodeAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action qrAuthenticationGenerateCodeAction() {
            return new QRAuthenticationGenerateCodeAction();
        }

    }
}
