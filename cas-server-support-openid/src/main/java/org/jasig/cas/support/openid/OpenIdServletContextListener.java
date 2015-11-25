package org.jasig.cas.support.openid;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * OpenID endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component
public class OpenIdServletContextListener extends AbstractServletContextInitializer {

    @Autowired
    @Qualifier("serviceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;

    @Autowired
    @Qualifier("openIdCredentialsAuthenticationHandler")
    private AuthenticationHandler openIdCredentialsAuthenticationHandler;

    @Autowired
    @Qualifier("openIdPrincipalResolver")
    private PrincipalResolver openIdPrincipalResolver;

    @Autowired
    @Qualifier("openIdServiceFactory")
    private OpenIdServiceFactory openIdServiceFactory;

    @Override
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(openIdCredentialsAuthenticationHandler, openIdPrincipalResolver);
    }

    @Override
    protected void initializeServletApplicationContext() {
        addControllerToCasServletHandlerMapping(OpenIdProtocolConstants.ENDPOINT_OPENID, "openIdProviderController");
        addServiceTicketUniqueIdGenerator(OpenIdService.class.getCanonicalName(), this.serviceTicketUniqueIdGenerator);
        addServiceFactory(openIdServiceFactory);
    }

    @Override
    protected void initializeServletContext(final ServletContextEvent event) {
        addEndpointMappingToCasServlet(event, OpenIdProtocolConstants.ENDPOINT_OPENID);
    }


}
