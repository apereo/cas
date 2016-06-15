package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.rest.CredentialFactory;
import org.apereo.cas.support.rest.DefaultCredentialFactory;
import org.apereo.cas.support.rest.TicketsResource;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * This is {@link RestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casRestConfiguration")
public class RestConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    @Qualifier("restAuthenticationThrottle")
    private HandlerInterceptor authenticationThrottle;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;


    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport =
            new DefaultAuthenticationSystemSupport();

    @Autowired(required = false)
    private CredentialFactory credentialFactory = new DefaultCredentialFactory();

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(this.authenticationThrottle).addPathPatterns("/v1/**");
    }

    @Bean
    public TicketsResource ticketResourceRestController() {
        final TicketsResource r = new TicketsResource();
        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCredentialFactory(credentialFactory);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWebApplicationServiceFactory(webApplicationServiceFactory);
        r.setCentralAuthenticationService(centralAuthenticationService);
        return r;
    }


}
