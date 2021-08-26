package org.apereo.cas.pm.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.ForgotUsernameCaptchaWebflowConfigurer;
import org.apereo.cas.pm.web.flow.ForgotUsernameWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.SendForgotUsernameInstructionsAction;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.SpringWebflowActionExecutionAuditablePrincipalResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordManagementForgotUsernameConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration("PasswordManagementForgotUsernameConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PasswordManagementForgotUsernameConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("returnValueResourceResolver")
    private ObjectProvider<AuditResourceResolver> returnValueResourceResolver;

    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
    private ObjectProvider<PasswordManagementService> passwordManagementService;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
    @Bean
    @RefreshScope
    public Action sendForgotUsernameInstructionsAction() {
        return new SendForgotUsernameInstructionsAction(casProperties, communicationsManager.getObject(),
            passwordManagementService.getObject(), defaultPrincipalResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "forgotUsernameWebflowConfigurer")
    @RefreshScope
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer forgotUsernameWebflowConfigurer() {
        return new ForgotUsernameWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "forgotUsernameAuditTrailRecordResolutionPlanConfigurer")
    public AuditTrailRecordResolutionPlanConfigurer forgotUsernameAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            plan.registerAuditActionResolver(AuditActionResolvers.REQUEST_FORGOT_USERNAME_ACTION_RESOLVER,
                new DefaultAuditActionResolver());
            plan.registerAuditResourceResolver(AuditResourceResolvers.REQUEST_FORGOT_USERNAME_RESOURCE_RESOLVER,
                returnValueResourceResolver.getObject());
            plan.registerAuditPrincipalResolver(AuditPrincipalResolvers.REQUEST_FORGOT_USERNAME_PRINCIPAL_RESOLVER,
                new SpringWebflowActionExecutionAuditablePrincipalResolver(SendForgotUsernameInstructionsAction.REQUEST_PARAMETER_EMAIL));
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "forgotUsernameCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer forgotUsernameCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(forgotUsernameWebflowConfigurer());
    }

    @ConditionalOnProperty(prefix = "cas.authn.pm.forgot-username.google-recaptcha", name = "enabled", havingValue = "true")
    @Configuration(value = "forgotUsernameCaptchaConfiguration", proxyBeanMethods = false)
    @DependsOn("passwordManagementWebflowConfigurer")
    public class ForgotUsernameCaptchaConfiguration {

        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaWebflowConfigurer")
        @RefreshScope
        @Bean
        public CasWebflowConfigurer forgotUsernameCaptchaWebflowConfigurer() {
            val configurer = new ForgotUsernameCaptchaWebflowConfigurer(
                flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                applicationContext, casProperties);
            configurer.setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 2);
            return configurer;
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_VALIDATE_CAPTCHA)
        @RefreshScope
        @Bean
        public Action forgotUsernameValidateCaptchaAction() {
            val recaptcha = casProperties.getAuthn().getPm().getForgotUsername().getGoogleRecaptcha();
            return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha));
        }

        @RefreshScope
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_INIT_CAPTCHA)
        public Action forgotUsernameInitializeCaptchaAction() {
            val recaptcha = casProperties.getAuthn().getPm().getForgotUsername().getGoogleRecaptcha();
            return new InitializeCaptchaAction(recaptcha) {
                @Override
                protected Event doExecute(final RequestContext requestContext) {
                    WebUtils.putRecaptchaForgotUsernameEnabled(requestContext, recaptcha);
                    return super.doExecute(requestContext);
                }
            };
        }

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer forgotUsernameCaptchaWebflowExecutionPlanConfigurer(
            @Qualifier("forgotUsernameCaptchaWebflowConfigurer") final CasWebflowConfigurer cfg) {
            return plan -> plan.registerWebflowConfigurer(cfg);
        }
    }
}
