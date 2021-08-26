package org.apereo.cas.pm.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.flow.PasswordManagementCaptchaWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementSingleSignOnParticipationStrategy;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.HandlePasswordExpirationWarningMessagesAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordResetAction;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction;
import org.apereo.cas.pm.web.flow.actions.ValidatePasswordResetTokenAction;
import org.apereo.cas.pm.web.flow.actions.VerifyPasswordResetRequestAction;
import org.apereo.cas.pm.web.flow.actions.VerifySecurityQuestionsAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;

/**
 * This is {@link PasswordManagementWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("passwordManagementWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class PasswordManagementWebflowConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("loginFlowExecutor")
    private ObjectProvider<FlowExecutor> loginFlowExecutor;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("passwordValidationService")
    private ObjectProvider<PasswordValidationService> passwordValidationService;

    @Autowired
    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
    private ObjectProvider<PasswordManagementService> passwordManagementService;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordManagementSingleSignOnParticipationStrategy")
    public SingleSignOnParticipationStrategy passwordManagementSingleSignOnParticipationStrategy() {
        return new PasswordManagementSingleSignOnParticipationStrategy(
            servicesManager.getObject(),
            ticketRegistrySupport.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            centralAuthenticationService.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordManagementSingleSignOnParticipationStrategyConfigurer")
    public SingleSignOnParticipationStrategyConfigurer passwordManagementSingleSignOnParticipationStrategyConfigurer() {
        return chain -> chain.addStrategy(passwordManagementSingleSignOnParticipationStrategy());
    }

    @RefreshScope
    @Bean
    public HandlerAdapter passwordResetHandlerAdapter() {
        val handler = new FlowHandlerAdapter() {
            @Override
            public boolean supports(final Object handler) {
                return super.supports(handler) && ((FlowHandler) handler)
                    .getFlowId().equals(PasswordManagementWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
            }
        };
        handler.setFlowExecutor(loginFlowExecutor.getObject());
        return handler;
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE)
    @RefreshScope
    @Bean
    public Action initPasswordChangeAction() {
        return new InitPasswordChangeAction(casProperties);
    }

    @ConditionalOnMissingBean(name = "initPasswordResetAction")
    @RefreshScope
    @Bean
    public Action initPasswordResetAction() {
        return new InitPasswordResetAction(passwordManagementService.getObject());
    }

    @ConditionalOnMissingBean(name = "passwordChangeAction")
    @RefreshScope
    @Bean
    public Action passwordChangeAction() {
        return new PasswordChangeAction(passwordManagementService.getObject(), passwordValidationService.getObject());
    }

    @ConditionalOnMissingBean(name = "sendPasswordResetInstructionsAction")
    @Bean
    @RefreshScope
    public Action sendPasswordResetInstructionsAction() {
        return new SendPasswordResetInstructionsAction(casProperties, communicationsManager.getObject(),
            passwordManagementService.getObject(), ticketRegistry.getObject(),
            ticketFactory.getObject(), defaultPrincipalResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "verifyPasswordResetRequestAction")
    @Bean
    @RefreshScope
    public Action verifyPasswordResetRequestAction() {
        return new VerifyPasswordResetRequestAction(casProperties,
            passwordManagementService.getObject(), centralAuthenticationService.getObject());
    }

    @ConditionalOnMissingBean(name = "handlePasswordExpirationWarningMessagesAction")
    @Bean
    @RefreshScope
    public Action handlePasswordExpirationWarningMessagesAction() {
        return new HandlePasswordExpirationWarningMessagesAction();
    }

    @ConditionalOnMissingBean(name = "verifySecurityQuestionsAction")
    @Bean
    @RefreshScope
    public Action verifySecurityQuestionsAction() {
        if (!casProperties.getAuthn().getPm().getReset().isSecurityQuestionsEnabled()) {
            LOGGER.debug("Functionality to handle security questions for password management is not enabled");
            return new StaticEventExecutionAction("success");
        }
        return new VerifySecurityQuestionsAction(passwordManagementService.getObject());
    }

    @ConditionalOnMissingBean(name = "validatePasswordResetTokenAction")
    @Bean
    @RefreshScope
    public Action validatePasswordResetTokenAction() {
        return new ValidatePasswordResetTokenAction(passwordManagementService.getObject(),
            centralAuthenticationService.getObject());
    }

    @ConditionalOnMissingBean(name = "passwordManagementWebflowConfigurer")
    @RefreshScope
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer passwordManagementWebflowConfigurer() {
        return new PasswordManagementWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "passwordManagementCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer passwordManagementCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(passwordManagementWebflowConfigurer());
    }

    @ConditionalOnProperty(prefix = "cas.authn.pm.google-recaptcha", name = "enabled", havingValue = "true")
    @Configuration(value = "passwordManagementCaptchaConfiguration", proxyBeanMethods = false)
    @DependsOn("passwordManagementWebflowConfigurer")
    public class PasswordManagementCaptchaConfiguration {

        @ConditionalOnMissingBean(name = "passwordManagementCaptchaWebflowConfigurer")
        @RefreshScope
        @Bean
        public CasWebflowConfigurer passwordManagementCaptchaWebflowConfigurer() {
            val configurer = new PasswordManagementCaptchaWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                applicationContext, casProperties);
            configurer.setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 1);
            return configurer;
        }

        @ConditionalOnMissingBean(name = "passwordResetValidateCaptchaAction")
        @RefreshScope
        @Bean
        public Action passwordResetValidateCaptchaAction() {
            val recaptcha = casProperties.getAuthn().getPm().getGoogleRecaptcha();
            return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha));
        }

        @RefreshScope
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT_CAPTCHA)
        public Action passwordResetInitializeCaptchaAction() {
            val recaptcha = casProperties.getAuthn().getPm().getGoogleRecaptcha();
            return new InitializeCaptchaAction(recaptcha) {
                @Override
                protected Event doExecute(final RequestContext requestContext) {
                    WebUtils.putRecaptchaPasswordManagementEnabled(requestContext, recaptcha);
                    return super.doExecute(requestContext);
                }
            };
        }

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "passwordManagementCaptchaWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer passwordManagementCaptchaWebflowExecutionPlanConfigurer(
            @Qualifier("passwordManagementCaptchaWebflowConfigurer") final CasWebflowConfigurer cfg) {
            return plan -> plan.registerWebflowConfigurer(cfg);
        }
    }
}
