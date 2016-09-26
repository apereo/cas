package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.handler.support.TokenAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.token.TokenAuthenticationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link TokenAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("tokenAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TokenAuthenticationConfiguration {


    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

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
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public PrincipalFactory tokenPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationHandler tokenAuthenticationHandler() {
        final TokenAuthenticationHandler h = new TokenAuthenticationHandler();
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getToken().getPrincipalTransformation()));
        h.setPrincipalFactory(tokenPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    public Action tokenAuthenticationAction() {
        final TokenAuthenticationAction a = new TokenAuthenticationAction();
        a.setAdaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy);
        a.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver);
        a.setServiceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver);
        a.setServicesManager(this.servicesManager);
        return a;
    }

    @PostConstruct
    public void initializeAuthenticationHandler() {
        this.authenticationHandlersResolvers.put(tokenAuthenticationHandler(),
                personDirectoryPrincipalResolver);
    }
}
