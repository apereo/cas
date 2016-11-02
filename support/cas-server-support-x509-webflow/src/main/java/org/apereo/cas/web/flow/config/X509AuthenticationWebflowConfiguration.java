package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.X509CertificateCredentialsNonInteractiveAction;
import org.apereo.cas.web.flow.X509WebflowConfigurer;
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
 * This is {@link X509AuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("x509AuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509AuthenticationWebflowConfiguration {

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired(required = false)
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired(required = false)
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "x509WebflowConfigurer")
    @Bean
    public CasWebflowConfigurer x509WebflowConfigurer() {
        final X509WebflowConfigurer w = new X509WebflowConfigurer();
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @Bean
    public Action x509Check() {
        final X509CertificateCredentialsNonInteractiveAction a = new X509CertificateCredentialsNonInteractiveAction();
        a.setAdaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy);
        a.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver);
        a.setServiceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver);
        return a;
    }
}
