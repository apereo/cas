package org.apereo.cas.pm.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.HandlePasswordExpirationWarningMessagesAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordResetAction;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction;
import org.apereo.cas.pm.web.flow.actions.VerifyPasswordResetRequestAction;
import org.apereo.cas.pm.web.flow.actions.VerifySecurityQuestionsAction;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
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
public class PasswordManagementWebflowConfiguration implements CasWebflowExecutionPlanConfigurer {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("loginFlowExecutor")
    private FlowExecutor loginFlowExecutor;

    @Autowired
    @Qualifier("passwordValidationService")
    private PasswordValidationService passwordValidationService;

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordManagementService;

    @RefreshScope
    @Bean
    public HandlerAdapter passwordResetHandlerAdapter() {
        final FlowHandlerAdapter handler = new FlowHandlerAdapter() {
            @Override
            public boolean supports(final Object handler) {
                return super.supports(handler) && ((FlowHandler) handler)
                    .getFlowId().equals(PasswordManagementWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
            }
        };
        handler.setFlowExecutor(loginFlowExecutor);
        return handler;
    }

    @ConditionalOnMissingBean(name = "initPasswordChangeAction")
    @RefreshScope
    @Bean
    public Action initPasswordChangeAction() {
        return new InitPasswordChangeAction(casProperties);
    }

    @ConditionalOnMissingBean(name = "initPasswordResetAction")
    @RefreshScope
    @Bean
    public Action initPasswordResetAction() {
        return new InitPasswordResetAction(passwordManagementService);
    }

    @ConditionalOnMissingBean(name = "passwordChangeAction")
    @RefreshScope
    @Bean
    public Action passwordChangeAction() {
        return new PasswordChangeAction(passwordManagementService, passwordValidationService);
    }

    @ConditionalOnMissingBean(name = "sendPasswordResetInstructionsAction")
    @Bean
    @RefreshScope
    public Action sendPasswordResetInstructionsAction() {
        return new SendPasswordResetInstructionsAction(casProperties, communicationsManager, passwordManagementService);
    }

    @ConditionalOnMissingBean(name = "verifyPasswordResetRequestAction")
    @Bean
    @RefreshScope
    public Action verifyPasswordResetRequestAction() {
        return new VerifyPasswordResetRequestAction(casProperties, passwordManagementService);
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
        return new VerifySecurityQuestionsAction(passwordManagementService);
    }

    @ConditionalOnMissingBean(name = "passwordManagementWebflowConfigurer")
    @RefreshScope
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer passwordManagementWebflowConfigurer() {
        return new PasswordManagementWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
            applicationContext, casProperties, initPasswordChangeAction());
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(passwordManagementWebflowConfigurer());
    }
}



