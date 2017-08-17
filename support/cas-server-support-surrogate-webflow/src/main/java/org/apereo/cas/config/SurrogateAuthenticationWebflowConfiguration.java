package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.SurrogateSelectionAction;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SurrogateAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Configuration("surrogateAuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateAuthenticationWebflowConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "surrogateWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer surrogateWebflowConfigurer() {
        return new SurrogateWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, selectSurrogateAction());
    }

    @ConditionalOnMissingBean(name = "selectSurrogateAction")
    @Bean
    public Action selectSurrogateAction() {
        return new SurrogateSelectionAction(casProperties.getAuthn().getSurrogate().getSeparator());
    }

    @Bean
    public Action authenticationViaFormAction() {
        return new SurrogateInitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                casProperties.getAuthn().getSurrogate().getSeparator());
    }
}
