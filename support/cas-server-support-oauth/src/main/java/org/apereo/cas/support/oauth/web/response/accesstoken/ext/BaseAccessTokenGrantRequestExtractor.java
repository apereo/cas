package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseAccessTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
public abstract class BaseAccessTokenGrantRequestExtractor {
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;
    /**
     * The Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;
    /**
     * The Request.
     */
    protected final HttpServletRequest request;
    /**
     * The Response.
     */
    protected final HttpServletResponse response;

    /**
     * OAuth settings.
     */
    protected final OAuthProperties oAuthProperties;
    
    /**
     * The Services manager.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    public BaseAccessTokenGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                final HttpServletRequest request, final HttpServletResponse response,
                                                final CentralAuthenticationService centralAuthenticationService,
                                                final OAuthProperties oAuthProperties) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.request = request;
        this.response = response;
        this.centralAuthenticationService = centralAuthenticationService;
        this.oAuthProperties = oAuthProperties;
    }

    /**
     * Extract access token request for grant.
     *
     * @return the access token request data holder
     */
    public abstract AccessTokenRequestDataHolder extract();

    /**
     * Supports grant type?
     *
     * @param context the context
     * @return true/false
     */
    public abstract boolean supports(HttpServletRequest context);
}
