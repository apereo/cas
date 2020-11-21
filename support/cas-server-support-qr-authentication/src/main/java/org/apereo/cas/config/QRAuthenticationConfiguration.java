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
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
@Configuration("QRAuthenticationConfiguration")
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class QRAuthenticationConfiguration implements WebSocketMessageBrokerConfigurer {
    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("tokenTicketJwtBuilder")
    private ObjectProvider<JwtBuilder> jwtBuilder;

    @Autowired
    @Qualifier("flowBuilderServices")
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Bean
    public QRAuthenticationChannelController qrAuthenticationChannelController(
        @Qualifier("brokerMessagingTemplate") final SimpMessagingTemplate template) {
        return new QRAuthenticationChannelController(template, qrAuthenticationTokenValidatorService());
    }

    @ConditionalOnMissingBean(name = "qrAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer qrAuthenticationWebflowConfigurer() {
        return new QRAuthenticationWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "qrAuthenticationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer qrAuthenticationCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(qrAuthenticationWebflowConfigurer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "qrAuthenticationValidateWebSocketChannelAction")
    @RefreshScope
    public Action qrAuthenticationValidateWebSocketChannelAction() {
        return new QRAuthenticationValidateTokenAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "qrAuthenticationGenerateCodeAction")
    @RefreshScope
    public Action qrAuthenticationGenerateCodeAction() {
        return new QRAuthenticationGenerateCodeAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "qrAuthenticationTokenValidatorService")
    @RefreshScope
    public QRAuthenticationTokenValidatorService qrAuthenticationTokenValidatorService() {
        return new DefaultQRAuthenticationTokenValidatorService(jwtBuilder.getObject(),
            centralAuthenticationService.getObject(), casProperties, qrAuthenticationDeviceRepository());
    }

    @Bean
    @ConditionalOnMissingBean(name = "qrAuthenticationDeviceRepository")
    @RefreshScope
    public QRAuthenticationDeviceRepository qrAuthenticationDeviceRepository() {
        val qr = casProperties.getAuthn().getQr();
        if (qr.getJson().getLocation() != null) {
            return new JsonResourceQRAuthenticationDeviceRepository(qr.getJson().getLocation());
        }
        return QRAuthenticationDeviceRepository.permitAll();
    }

    @ConditionalOnMissingBean(name = "qrAuthenticationPrincipalFactory")
    @Bean
    public PrincipalFactory qrAuthenticationPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "qrAuthenticationTokenAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler qrAuthenticationTokenAuthenticationHandler() {
        return new QRAuthenticationTokenAuthenticationHandler(
            servicesManager.getObject(),
            qrAuthenticationPrincipalFactory(),
            qrAuthenticationTokenValidatorService());
    }

    @ConditionalOnMissingBean(name = "casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(qrAuthenticationTokenAuthenticationHandler());
            plan.registerAuthenticationHandlerResolver(
                new ByCredentialTypeAuthenticationHandlerResolver(QRAuthenticationTokenCredential.class));
        };
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public QRAuthenticationDeviceRepositoryEndpoint qrAuthenticationDeviceRepositoryEndpoint() {
        return new QRAuthenticationDeviceRepositoryEndpoint(casProperties, qrAuthenticationDeviceRepository());
    }

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
}



