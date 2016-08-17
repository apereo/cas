package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.PasswordValidator;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.InitPasswordChangeAction;
import org.apereo.cas.web.flow.PasswordChangeAction;
import org.apereo.cas.web.flow.PasswordManagementWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.thymeleaf.spring4.view.ThymeleafView;

/**
 * This is {@link PasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("passwordManagementConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PasswordManagementConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Bean
    public View passwordChangeView() {
        return new PasswordChangeView();
    }

    @Bean
    public Action initPasswordChangeAction() {
        return new InitPasswordChangeAction(casProperties.getAuthn().getPm());
    }

    @Bean
    public Action passwordChangeAction() {
        return new PasswordChangeAction();
    }

    @Bean
    public CasWebflowConfigurer passwordManagementWebflowConfigurer() {
        final PasswordManagementWebflowConfigurer w = new PasswordManagementWebflowConfigurer();
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @ConditionalOnMissingBean(name = "PasswordValidator")
    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator(casProperties.getAuthn().getPm().getPolicyPattern());
    }

    private static class PasswordChangeView extends ThymeleafView {
    }
}



