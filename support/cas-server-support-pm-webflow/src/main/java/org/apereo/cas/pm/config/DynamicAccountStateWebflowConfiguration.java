package org.apereo.cas.pm.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.web.flow.DynamicAccountStatusWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.acct.BaseAccountStateCheckAction;
import org.apereo.cas.pm.web.flow.actions.acct.GroovyAccountStateCheckAction;
import org.apereo.cas.pm.web.flow.actions.acct.RestfulAccountStateCheckAction;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link DynamicAccountStateWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("dynamicAccountStateWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamicAccountStateWebflowConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordManagementConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;
    
    @ConditionalOnMissingBean(name = "dynamicAccountStatusWebflowConfigurer")
    @RefreshScope
    @Bean
    public CasWebflowConfigurer dynamicAccountStatusWebflowConfigurer() {
        return new DynamicAccountStatusWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @ConditionalOnMissingBean(name = "checkAccountStateAction")
    @RefreshScope
    @Bean
    public Action checkAccountStateAction() {
        final PasswordManagementProperties.AccountStatus status = casProperties.getAuthn().getPm().getAccountStatus();
        if (StringUtils.isNotBlank(status.getGroovy().getScript())) {
            return new GroovyAccountStateCheckAction(status.getGroovy().getScript());
        }
        if (StringUtils.isNotBlank(status.getRest().getEndpoint())) {
            return new RestfulAccountStateCheckAction(new RestTemplate(), status.getRest().getEndpoint());
        }
        return new BaseAccountStateCheckAction();
    }
}
