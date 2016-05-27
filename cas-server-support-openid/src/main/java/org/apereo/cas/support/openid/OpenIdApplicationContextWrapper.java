package org.apereo.cas.support.openid;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.openid.authentication.principal.OpenIdService;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initializes the CAS root servlet context to make sure
 * OpenID endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("openIdApplicationContextWrapper")
public class OpenIdApplicationContextWrapper extends BaseApplicationContextWrapper {

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

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(this.openIdCredentialsAuthenticationHandler, this.openIdPrincipalResolver);
        addServiceTicketUniqueIdGenerator(OpenIdService.class.getCanonicalName(), this.serviceTicketUniqueIdGenerator);
        addServiceFactory(this.openIdServiceFactory);
    }

}
