package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.PasswordChangeService;
import org.apereo.cas.web.PasswordValidator;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.InitPasswordChangeAction;
import org.apereo.cas.web.flow.PasswordChangeAction;
import org.apereo.cas.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.web.ldap.LdapPasswordChangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

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

    @RefreshScope
    @Bean
    public Action initPasswordChangeAction() {
        return new InitPasswordChangeAction();
    }

    @RefreshScope
    @Bean
    public Action passwordChangeAction() {
        return new PasswordChangeAction(passwordChangeService());
    }

    @ConditionalOnMissingBean(name="passwordChangeService")
    @RefreshScope
    @Bean
    public PasswordChangeService passwordChangeService() {
        if (casProperties.getAuthn().getPm().isEnabled() 
            && StringUtils.isNotBlank(casProperties.getAuthn().getPm().getLdap().getLdapUrl())
            && StringUtils.isNotBlank(casProperties.getAuthn().getPm().getLdap().getBaseDn())
            && StringUtils.isNotBlank(casProperties.getAuthn().getPm().getLdap().getUserFilter())) {
            return new LdapPasswordChangeService();
        }
        if (casProperties.getAuthn().getPm().isEnabled()) {
            LOGGER.warn("No backend is configured to handle the account update operation. Verify your settings");
        }
        return (c, bean) -> false;
    }

    @ConditionalOnMissingBean(name="passwordManagementWebflowConfigurer")
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



