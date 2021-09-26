package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.flow.CasCaptchaWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasCaptchaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.google-recaptcha", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "casCaptchaConfiguration", proxyBeanMethods = false)
public class CasCaptchaConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "captchaWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer captchaWebflowConfigurer(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                         @Qualifier("loginFlowDefinitionRegistry")
                                                         final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                         @Qualifier("flowBuilderServices")
                                                         final FlowBuilderServices flowBuilderServices) {
        return new CasCaptchaWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "captchaValidator")
    @Autowired
    public CaptchaValidator captchaValidator(final CasConfigurationProperties casProperties) {
        return CaptchaValidator.getInstance(casProperties.getGoogleRecaptcha());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "validateCaptchaAction")
    public Action validateCaptchaAction(
        @Qualifier("captchaValidator")
        final CaptchaValidator captchaValidator) {
        return new ValidateCaptchaAction(captchaValidator);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "initializeCaptchaAction")
    @Autowired
    public Action initializeCaptchaAction(final CasConfigurationProperties casProperties) {
        return new InitializeCaptchaAction(casProperties.getGoogleRecaptcha()) {

            @Override
            protected Event doExecute(final RequestContext requestContext) {
                requestContext.getFlowScope().put("recaptchaLoginEnabled", googleRecaptchaProperties.isEnabled());
                return super.doExecute(requestContext);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "captchaCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer captchaCasWebflowExecutionPlanConfigurer(
        @Qualifier("captchaWebflowConfigurer")
        final CasWebflowConfigurer captchaWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(captchaWebflowConfigurer);
    }
}
