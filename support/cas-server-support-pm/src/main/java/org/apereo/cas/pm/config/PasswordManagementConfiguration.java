package org.apereo.cas.pm.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.NoOpPasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetTokenCipherExecutor;
import org.apereo.cas.pm.PasswordValidator;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.InitPasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordResetAction;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction;
import org.apereo.cas.pm.web.flow.actions.VerifyPasswordResetRequestAction;
import org.apereo.cas.pm.web.flow.actions.VerifySecurityQuestionsAction;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * This is {@link PasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("passwordManagementConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PasswordManagementConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordManagementConfiguration.class);

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

    @RefreshScope
    @Bean
    public Action initPasswordChangeAction() {
        return new InitPasswordChangeAction();
    }

    @ConditionalOnMissingBean(name = "initPasswordResetAction")
    @Autowired
    @RefreshScope
    @Bean
    public Action initPasswordResetAction(@Qualifier("passwordChangeService") final PasswordManagementService passwordManagementService) {
        return new InitPasswordResetAction(passwordManagementService);
    }

    @ConditionalOnMissingBean(name = "passwordChangeAction")
    @RefreshScope
    @Bean
    public Action passwordChangeAction() {
        return new PasswordChangeAction(passwordChangeService());
    }

    @ConditionalOnMissingBean(name = "passwordManagementCipherExecutor")
    @RefreshScope
    @Bean
    public CipherExecutor<Serializable, String> passwordManagementCipherExecutor() {
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        if (pm.isEnabled()) {
            return new PasswordResetTokenCipherExecutor(
                    pm.getReset().getSecurity().getEncryptionKey(),
                    pm.getReset().getSecurity().getSigningKey());
        }
        return NoOpCipherExecutor.getInstance();
    }

    @ConditionalOnMissingBean(name = "passwordChangeService")
    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        if (pm.isEnabled()) {
            LOGGER.warn("No storage service (LDAP, Database, etc) is configured to handle the account update and password service operations. "
                    + "Password management functionality will have no effect and will be disabled until a storage service is configured. "
                    + "To explicitly disable the password management functionality, add 'cas.authn.pm.enabled=false' to the CAS configuration");
        }
        return new NoOpPasswordManagementService(passwordManagementCipherExecutor(),
                casProperties.getServer().getPrefix(),
                casProperties.getAuthn().getPm());
    }


    @ConditionalOnMissingBean(name = "sendPasswordResetInstructionsAction")
    @Autowired
    @Bean
    public Action sendPasswordResetInstructionsAction(@Qualifier("passwordChangeService") final PasswordManagementService passwordManagementService) {
        return new SendPasswordResetInstructionsAction(communicationsManager, passwordManagementService);
    }

    @ConditionalOnMissingBean(name = "verifyPasswordResetRequestAction")
    @Bean
    public Action verifyPasswordResetRequestAction(@Qualifier("passwordChangeService") final PasswordManagementService passwordManagementService) {
        return new VerifyPasswordResetRequestAction(passwordManagementService);
    }

    @ConditionalOnMissingBean(name = "verifySecurityQuestionsAction")
    @Bean
    public Action verifySecurityQuestionsAction(@Qualifier("passwordChangeService") final PasswordManagementService passwordManagementService) {
        return new VerifySecurityQuestionsAction(passwordManagementService);
    }

    @ConditionalOnMissingBean(name = "passwordManagementWebflowConfigurer")
    @RefreshScope
    @Bean
    public CasWebflowConfigurer passwordManagementWebflowConfigurer() {
        return new PasswordManagementWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordValidator")
    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @PostConstruct
    public void init() {
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();
        if (pm.isEnabled()) {
            if (!communicationsManager.isMailSenderDefined()) {
                LOGGER.warn("CAS is unable to send password-reset emails given no settings are defined to account for email servers, etc");
            }
            if (!communicationsManager.isSmsSenderDefined()) {
                LOGGER.warn("CAS is unable to send password-reset sms messages given no settings are defined to account for sms providers, etc");
            }
        }
    }
}



