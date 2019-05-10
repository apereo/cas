package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeAuthenticationHandler;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeAuthenticationWebflowConfigurer;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeCredential;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodePrepareAuthenticationAction;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeValidateWebSocketChannelAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link AccepttoAuthenticationQRCodeConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("accepttoMultifactorAuthenticationQRCodeConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnProperty(value = "cas.authn.mfa.acceptto.qrLoginEnabled", havingValue = "true")
public class AccepttoAuthenticationQRCodeConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @ConditionalOnMissingBean(name = "mfaAccepttoQRCodeCasWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer mfaAccepttoQRCodeCasWebflowExecutionPlanConfigurer() {
        return new CasWebflowExecutionPlanConfigurer() {
            @Override
            public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
                plan.registerWebflowConfigurer(mfaAccepttoQRCodeAuthenticationWebflowConfigurer());
            }
        };
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoQRCodeAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaAccepttoQRCodeAuthenticationWebflowConfigurer() {
        return new AccepttoQRCodeAuthenticationWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry.getIfAvailable(),
            applicationContext,
            casProperties);
    }

    @Bean
    public Action initializeLoginAction() {
        return new AccepttoQRCodePrepareAuthenticationAction(servicesManager.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "mfaAccepttoQRCodeValidateWebSocketChannelAction")
    public Action mfaAccepttoQRCodeValidateWebSocketChannelAction() {
        return new AccepttoQRCodeValidateWebSocketChannelAction(casProperties, authenticationSystemSupport.getIfAvailable());
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
                + "is defined for the Acceptto integration. Examine your CAS configuration and adjust.");
        }
        return new AccepttoQRCodeAuthenticationHandler(
            servicesManager.getIfAvailable(),
            casAccepttoQRCodePrincipalFactory());
    }

    @ConditionalOnMissingBean(name = "casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer casAccepttoAuthenticationQRCodeEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(casAccepttoQRCodeAuthenticationHandler());
            plan.registerAuthenticationHandlerResolver(
                new ByCredentialTypeAuthenticationHandlerResolver(AccepttoQRCodeCredential.class));
        };
    }
}
