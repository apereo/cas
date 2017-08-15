package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Controller
public abstract class BaseOAuth20Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOAuth20Controller.class);

    /**
     * Collection of CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * Convert profile scopes to attributes.
     */
    protected final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Cookie retriever.
     */
    protected final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    /**
     * The Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;
    /**
     * The Validator.
     */
    protected final OAuth20Validator validator;
    /**
     * The Access token factory.
     */
    protected final AccessTokenFactory accessTokenFactory;
    /**
     * The Principal factory.
     */
    protected final PrincipalFactory principalFactory;
    /**
     * The Web application service service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;


    /**
     * Instantiates a new Base o auth 20 controller.
     *
     * @param servicesManager                     the services manager
     * @param ticketRegistry                      the ticket registry
     * @param validator                           the validator
     * @param accessTokenFactory                  the access token factory
     * @param principalFactory                    the principal factory
     * @param webApplicationServiceServiceFactory the web application service service factory
     * @param scopeToAttributesFilter             the scope to attributes filter
     * @param casProperties                       the cas properties
     * @param ticketGrantingTicketCookieGenerator the ticket granting ticket cookie generator
     */
    public BaseOAuth20Controller(final ServicesManager servicesManager,
                                 final TicketRegistry ticketRegistry,
                                 final OAuth20Validator validator,
                                 final AccessTokenFactory accessTokenFactory,
                                 final PrincipalFactory principalFactory,
                                 final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                 final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                 final CasConfigurationProperties casProperties,
                                 final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.validator = validator;
        this.accessTokenFactory = accessTokenFactory;
        this.principalFactory = principalFactory;
        this.webApplicationServiceServiceFactory = webApplicationServiceServiceFactory;
        this.casProperties = casProperties;
        this.scopeToAttributesFilter = scopeToAttributesFilter;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }
}
