package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.Pac4jErrorViewResolver;
import org.apereo.cas.web.flow.Pac4jWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link Pac4jWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("pac4jWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Pac4jWebflowConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "pac4jWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer pac4jWebflowConfigurer() {
        final Pac4jWebflowConfigurer r = new Pac4jWebflowConfigurer();
        r.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        r.setFlowBuilderServices(flowBuilderServices);
        return r;
    }

    @Bean
    public ErrorViewResolver pac4jErrorViewResolver() {
        return new Pac4jErrorViewResolver();
    }
}
