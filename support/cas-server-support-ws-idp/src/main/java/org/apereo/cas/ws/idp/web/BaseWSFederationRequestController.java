package org.apereo.cas.ws.idp.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Date;

/**
 * This is {@link BaseWSFederationRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Controller
public abstract class BaseWSFederationRequestController {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWSFederationRequestController.class);

    /**
     * The Identity provider configuration service.
     */
    protected final IdentityProviderConfigurationService identityProviderConfigurationService;

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * The Web application service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    /**
     * The Callback service.
     */
    protected final Service callbackService;

    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * The Service selection strategy.
     */
    protected final AuthenticationServiceSelectionStrategy serviceSelectionStrategy;

    /**
     * The Http client.
     */
    protected final HttpClient httpClient;

    /**
     * The Security token ticket factory.
     */
    protected final SecurityTokenTicketFactory securityTokenTicketFactory;

    /**
     * The Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * The Ticket granting ticket cookie generator.
     */
    protected final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    /**
     * The Ticket registry support.
     */
    protected final TicketRegistrySupport ticketRegistrySupport;

    /**
     * Instantiates a new Base ws federation request controller.
     *
     * @param identityProviderConfigurationService the identity provider configuration service
     * @param servicesManager                      the services manager
     * @param webApplicationServiceFactory         the web application service factory
     * @param casProperties                        the cas properties
     * @param serviceSelectionStrategy             the service selection strategy
     * @param httpClient                           the http client
     * @param securityTokenTicketFactory           the security token ticket factory
     * @param ticketRegistry                       the ticket registry
     * @param ticketGrantingTicketCookieGenerator  the ticket granting ticket cookie generator
     * @param ticketRegistrySupport                the ticket registry support
     */
    public BaseWSFederationRequestController(final IdentityProviderConfigurationService identityProviderConfigurationService,
                                             final ServicesManager servicesManager,
                                             final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                             final CasConfigurationProperties casProperties,
                                             final AuthenticationServiceSelectionStrategy serviceSelectionStrategy,
                                             final HttpClient httpClient,
                                             final SecurityTokenTicketFactory securityTokenTicketFactory,
                                             final TicketRegistry ticketRegistry,
                                             final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                             final TicketRegistrySupport ticketRegistrySupport) {
        this.identityProviderConfigurationService = identityProviderConfigurationService;
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.casProperties = casProperties;
        this.serviceSelectionStrategy = serviceSelectionStrategy;
        this.httpClient = httpClient;
        this.securityTokenTicketFactory = securityTokenTicketFactory;
        this.ticketRegistry = ticketRegistry;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistrySupport = ticketRegistrySupport;
        this.callbackService = registerCallback(WSFederationConstants.ENDPOINT_FEDERATION_REQUEST_CALLBACK);
    }

    /**
     * Register callback service.
     *
     * @param callbackUrl the callback url
     * @return the service
     */
    private Service registerCallback(final String callbackUrl) {
        final Service callbackService = this.webApplicationServiceFactory.createService(callbackUrl);
        if (!this.servicesManager.matchesExistingService(callbackService)) {
            LOGGER.debug("Initializing callback service [{}]", callbackService);

            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(Math.abs(new SecureRandom().nextLong()));
            service.setEvaluationOrder(0);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("WS-Federation Authentication Request");
            service.setServiceId(callbackService.getId().concat(".+"));

            LOGGER.debug("Saving callback service [{}] into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.load();
        }
        return callbackService;
    }

    /**
     * Construct service url string.
     *
     * @param request      the request
     * @param response     the response
     * @param wsfedRequest the ws federation request
     * @return the service url
     */
    protected String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                         final WSFederationRequest wsfedRequest) {
        try {
            final URIBuilder builder = new URIBuilder(this.callbackService.getId());

            builder.addParameter(WSFederationConstants.WA, wsfedRequest.getWa());
            builder.addParameter(WSFederationConstants.WREPLY, wsfedRequest.getWreply());
            builder.addParameter(WSFederationConstants.WTREALM, wsfedRequest.getWtrealm());

            if (StringUtils.isNotBlank(wsfedRequest.getWctx())) {
                builder.addParameter(WSFederationConstants.WCTX, wsfedRequest.getWctx());
            }
            if (StringUtils.isNotBlank(wsfedRequest.getWfresh())) {
                builder.addParameter(WSFederationConstants.WREFRESH, wsfedRequest.getWfresh());
            }
            if (StringUtils.isNotBlank(wsfedRequest.getWhr())) {
                builder.addParameter(WSFederationConstants.WHR, wsfedRequest.getWhr());
            }
            if (StringUtils.isNotBlank(wsfedRequest.getWreq())) {
                builder.addParameter(WSFederationConstants.WREQ, wsfedRequest.getWreq());
            }

            final URI url = builder.build();

            LOGGER.debug("Built service callback url [{}]", url);
            return org.jasig.cas.client.util.CommonUtils.constructServiceUrl(request, response,
                    url.toString(), casProperties.getServer().getName(),
                    CasProtocolConstants.PARAMETER_SERVICE,
                    CasProtocolConstants.PARAMETER_TICKET, false);
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }

    /**
     * Should redirect for authentication ?
     *
     * @param fedRequest the fed request
     * @param response   the response
     * @param request    the request
     * @return the boolean
     */
    protected boolean shouldRedirectForAuthentication(final WSFederationRequest fedRequest,
                                                      final HttpServletResponse response,
                                                      final HttpServletRequest request) {
        return isSecurityTokenExpired(fedRequest, response, request)
                || isAuthenticationRequired(fedRequest, response, request);
    }

    /**
     * Is security token expired ?
     *
     * @param fedRequest the fed request
     * @param response   the response
     * @param request    the request
     * @return the boolean
     */
    protected boolean isSecurityTokenExpired(final WSFederationRequest fedRequest,
                                             final HttpServletResponse response,
                                             final HttpServletRequest request) {
        final SecurityToken idpToken = getSecurityTokenFromRequest(request);
        if (idpToken == null) {
            return true;
        }

        return idpToken.isExpired();
    }

    /**
     * Gets security token from request.
     *
     * @param request the request
     * @return the security token from request
     */
    protected SecurityToken getSecurityTokenFromRequest(final HttpServletRequest request) {
        final String cookieValue = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isNotBlank(cookieValue)) {
            final String sts = securityTokenTicketFactory.createLinkedId(cookieValue);
            final SecurityTokenTicket stt = ticketRegistry.getTicket(sts, SecurityTokenTicket.class);
            if (stt == null) {
                return null;
            }
            if (stt.isExpired()) {
                return null;
            }
            return stt.getSecurityToken();
        }
        return null;
    }

    /**
     * Gets ticket granting ticket from request.
     *
     * @param request the request
     * @return the ticket granting ticket from request
     */
    protected TicketGrantingTicket getTicketGrantingTicketFromRequest(final HttpServletRequest request) {
        final String cookieValue = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isNotBlank(cookieValue)) {
            final TicketGrantingTicket tgt = ticketRegistry.getTicket(cookieValue, TicketGrantingTicket.class);
            if (tgt != null && !tgt.isExpired()) {
                return tgt;
            }
        }
        return null;
    }


    /**
     * Is authentication required?
     *
     * @param fedRequest the fed request
     * @param response   the response
     * @param request    the request
     * @return the boolean
     */
    protected boolean isAuthenticationRequired(final WSFederationRequest fedRequest,
                                               final HttpServletResponse response,
                                               final HttpServletRequest request) {
        if (StringUtils.isBlank(fedRequest.getWfresh()) || NumberUtils.isCreatable(fedRequest.getWfresh())) {
            return false;
        }

        final long ttl = Long.parseLong(fedRequest.getWfresh().trim());
        if (ttl == 0) {
            return true;
        }

        final SecurityToken idpToken = getSecurityTokenFromRequest(request);
        if (idpToken == null) {
            return true;
        }

        final long ttlMs = ttl * 60L * 1000L;
        if (ttlMs > 0) {
            final Date createdDate = idpToken.getCreated();
            if (createdDate != null) {
                final Date expiryDate = new Date();
                expiryDate.setTime(createdDate.getTime() + ttlMs);
                if (expiryDate.before(new Date())) {
                    return true;
                }
            }
        }
        return false;
    }
}
