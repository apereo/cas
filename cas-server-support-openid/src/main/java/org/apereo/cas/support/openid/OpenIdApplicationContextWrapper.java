package org.apereo.cas.support.openid;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.openid.authentication.principal.OpenIdService;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.web.BaseApplicationContextWrapper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Initializes the CAS root servlet context to make sure
 * OpenID endpoint can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Resource(name="serviceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;

    @Resource(name="openIdCredentialsAuthenticationHandler")
    private AuthenticationHandler openIdCredentialsAuthenticationHandler;

    @Resource(name="openIdPrincipalResolver")
    private PrincipalResolver openIdPrincipalResolver;

    @Resource(name="openIdServiceFactory")
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
