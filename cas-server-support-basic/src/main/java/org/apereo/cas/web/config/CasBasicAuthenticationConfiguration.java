package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.BasicAuthenticationAction;
import org.apereo.cas.web.flow.BasicAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasBasicAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casBasicAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasBasicAuthenticationConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Bean
    public Action basicAuthenticationAction() {
        final BasicAuthenticationAction a = new BasicAuthenticationAction();

        a.setAuthenticationSystemSupport(authenticationSystemSupport);
        a.setCentralAuthenticationService(centralAuthenticationService);
        a.setPrincipalFactory(basicPrincipalFactory());
        a.setWarnCookieGenerator(warnCookieGenerator);
        return a;
    }

    @ConditionalOnMissingBean(name="basicAuthenticationWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer basicAuthenticationWebflowConfigurer() {
        final BasicAuthenticationWebflowConfigurer w =
                new BasicAuthenticationWebflowConfigurer();
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @Bean
    public PrincipalFactory basicPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }
}
