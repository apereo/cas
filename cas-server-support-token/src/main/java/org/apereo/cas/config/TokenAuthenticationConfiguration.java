package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.handler.support.TokenAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.token.TokenAuthenticationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link TokenAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("tokenAuthenticationConfiguration")
public class TokenAuthenticationConfiguration {

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;


    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("tokenPrincipalNameTransformer")
    private PrincipalNameTransformer principalNameTransformer;

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
        if (principalNameTransformer != null) {
            h.setPrincipalNameTransformer(principalNameTransformer);
        }
        h.setPrincipalFactory(tokenPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    public Action tokenAuthenticationAction() {
        final TokenAuthenticationAction a = new TokenAuthenticationAction();
        a.setAuthenticationSystemSupport(this.authenticationSystemSupport);
        a.setCentralAuthenticationService(this.centralAuthenticationService);
        return a;
    }
}
