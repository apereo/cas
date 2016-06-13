package org.apereo.cas.support.openid;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.openid.authentication.principal.OpenIdService;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;

/**
 * Initializes the CAS root servlet context to make sure
 * OpenID endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdApplicationContextWrapper extends BaseApplicationContextWrapper {
    private AuthenticationHandler openIdCredentialsAuthenticationHandler;
    
    private PrincipalResolver openIdPrincipalResolver;

    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;
    
    private OpenIdServiceFactory openIdServiceFactory;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(this.openIdCredentialsAuthenticationHandler, this.openIdPrincipalResolver);
        addServiceTicketUniqueIdGenerator(OpenIdService.class.getCanonicalName(), this.serviceTicketUniqueIdGenerator);
        addServiceFactory(this.openIdServiceFactory);
    }

    public void setServiceTicketUniqueIdGenerator(final UniqueTicketIdGenerator serviceTicketUniqueIdGenerator) {
        this.serviceTicketUniqueIdGenerator = serviceTicketUniqueIdGenerator;
    }

    public void setOpenIdCredentialsAuthenticationHandler(final AuthenticationHandler openIdCredentialsAuthenticationHandler) {
        this.openIdCredentialsAuthenticationHandler = openIdCredentialsAuthenticationHandler;
    }

    public void setOpenIdPrincipalResolver(final PrincipalResolver openIdPrincipalResolver) {
        this.openIdPrincipalResolver = openIdPrincipalResolver;
    }

    public void setOpenIdServiceFactory(final OpenIdServiceFactory openIdServiceFactory) {
        this.openIdServiceFactory = openIdServiceFactory;
    }
}
