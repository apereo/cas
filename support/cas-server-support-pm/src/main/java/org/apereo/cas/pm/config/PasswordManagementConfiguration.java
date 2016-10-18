package org.apereo.cas.pm.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetTokenCipherExecutor;
import org.apereo.cas.pm.PasswordValidator;
import org.apereo.cas.pm.ldap.LdapPasswordManagementService;
import org.apereo.cas.pm.web.flow.InitPasswordChangeAction;
import org.apereo.cas.pm.web.flow.InitPasswordResetAction;
import org.apereo.cas.pm.web.flow.PasswordChangeAction;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.SendPasswordResetInstructionsAction;
import org.apereo.cas.pm.web.flow.VerifyPasswordResetRequestAction;
import org.apereo.cas.pm.web.flow.VerifySecurityQuestionsAction;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
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

    @Autowired
    @RefreshScope
    @Bean
    public Action initPasswordResetAction(@Qualifier("passwordChangeService")
                                          final PasswordManagementService passwordManagementService) {
        return new InitPasswordResetAction(passwordManagementService);
    }

    @RefreshScope
    @Bean
    public Action passwordChangeAction() {
        return new PasswordChangeAction(passwordChangeService());
    }

    @RefreshScope
    @Bean
    public CipherExecutor<String, String> passwordManagementCipherExecutor() {
        if (casProperties.getAuthn().getPm().isEnabled()) {
            return new PasswordResetTokenCipherExecutor(
                    casProperties.getAuthn().getPm().getReset().getSecurity().getEncryptionKey(),
                    casProperties.getAuthn().getPm().getReset().getSecurity().getSigningKey());
        }
        return new NoOpCipherExecutor();
    }

    @ConditionalOnMissingBean(name = "passwordChangeService")
    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        if (casProperties.getAuthn().getPm().isEnabled()
                && StringUtils.isNotBlank(casProperties.getAuthn().getPm().getLdap().getLdapUrl())
                && StringUtils.isNotBlank(casProperties.getAuthn().getPm().getLdap().getBaseDn())
                && StringUtils.isNotBlank(casProperties.getAuthn().getPm().getLdap().getUserFilter())) {
            return new LdapPasswordManagementService(passwordManagementCipherExecutor());
        }

        if (casProperties.getAuthn().getPm().isEnabled()) {
            LOGGER.warn("No backend is configured to handle the account update operations. Verify your settings");
        }
        return new PasswordManagementService() {
        };
    }

    @Autowired
    @Bean
    public Action sendPasswordResetInstructionsAction(@Qualifier("passwordChangeService")
                                                      final PasswordManagementService passwordManagementService) {
        return new SendPasswordResetInstructionsAction(passwordManagementService);
    }

    @Bean
    public Action verifyPasswordResetRequestAction(@Qualifier("passwordChangeService")
                                                   final PasswordManagementService passwordManagementService) {
        return new VerifyPasswordResetRequestAction(passwordManagementService);
    }

    @Bean
    public Action verifySecurityQuestionsAction(@Qualifier("passwordChangeService")
                                                final PasswordManagementService passwordManagementService) {
        return new VerifySecurityQuestionsAction(passwordManagementService);
    }

    @ConditionalOnMissingBean(name = "passwordManagementWebflowConfigurer")
    @RefreshScope
    @Bean
    public CasWebflowConfigurer passwordManagementWebflowConfigurer() {
        final PasswordManagementWebflowConfigurer w = new PasswordManagementWebflowConfigurer();
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordValidator")
    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }
}



