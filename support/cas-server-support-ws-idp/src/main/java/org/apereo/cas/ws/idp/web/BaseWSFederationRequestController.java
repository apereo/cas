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
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;
import java.time.Instant;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseWSFederationRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Controller
public abstract class BaseWSFederationRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWSFederationRequestController.class);

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
     * @param servicesManager                     the services manager
     * @param webApplicationServiceFactory        the web application service factory
     * @param casProperties                       the cas properties
     * @param serviceSelectionStrategy            the service selection strategy
     * @param httpClient                          the http client
     * @param securityTokenTicketFactory          the security token ticket factory
     * @param ticketRegistry                      the ticket registry
     * @param ticketGrantingTicketCookieGenerator the ticket granting ticket cookie generator
     * @param ticketRegistrySupport               the ticket registry support
     */
    public BaseWSFederationRequestController(
            final ServicesManager servicesManager,
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final CasConfigurationProperties casProperties,
            final AuthenticationServiceSelectionStrategy serviceSelectionStrategy,
            final HttpClient httpClient,
            final SecurityTokenTicketFactory securityTokenTicketFactory,
            final TicketRegistry ticketRegistry,
            final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
            final TicketRegistrySupport ticketRegistrySupport) {
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
            service.setId(Math.abs(RandomUtils.getInstanceNative().nextLong()));
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

            LOGGER.trace("Built service callback url [{}]", url);
            return org.jasig.cas.client.util.CommonUtils.constructServiceUrl(request, response,
                    url.toString(), casProperties.getServer().getName(),
                    CasProtocolConstants.PARAMETER_SERVICE,
                    CasProtocolConstants.PARAMETER_TICKET, false);
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
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
            final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(cookieValue, TicketGrantingTicket.class);
            if (tgt != null) {
                final String sts = tgt.getDescendantTickets().stream()
                        .filter(t -> t.startsWith(SecurityTokenTicket.PREFIX))
                        .findFirst()
                        .orElse(null);
                if (StringUtils.isNotBlank(sts)) {
                    final SecurityTokenTicket stt = ticketRegistry.getTicket(sts, SecurityTokenTicket.class);
                    if (stt == null || stt.isExpired()) {
                        LOGGER.warn("Security token ticket [{}] is not found or has expired", sts);
                        return null;
                    }
                    if (stt.getSecurityToken().isExpired()) {
                        LOGGER.warn("Security token linked to ticket [{}] has expired", sts);
                        return null;
                    }
                    return stt.getSecurityToken();
                }
            }
        }
        return null;
    }


    /**
     * Is authentication required?
     *
     * @param fedRequest the fed request
     * @param request    the request
     * @return the boolean
     */
    protected boolean shouldRenewAuthentication(final WSFederationRequest fedRequest,
                                                final HttpServletRequest request) {
        if (StringUtils.isBlank(fedRequest.getWfresh()) || NumberUtils.isCreatable(fedRequest.getWfresh())) {
            return false;
        }
        final long ttl = Long.parseLong(fedRequest.getWfresh().trim());
        if (ttl == 0) {
            return false;
        }

        final SecurityToken idpToken = getSecurityTokenFromRequest(request);
        if (idpToken == null) {
            return true;
        }

        final long ttlMs = ttl * 60L * 1000L;
        if (ttlMs > 0) {
            final Instant createdDate = idpToken.getCreated();
            if (createdDate != null) {
                final Date expiryDate = new Date();
                expiryDate.setTime(createdDate.toEpochMilli() + ttlMs);
                if (expiryDate.before(new Date())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets ws federation registered service.
     *
     * @param response   the response
     * @param request    the request
     * @param fedRequest the fed request
     * @return the ws federation registered service
     */
    protected WSFederationRegisteredService findAndValidateFederationRequestForRegisteredService(final HttpServletResponse response,
                                                                                                 final HttpServletRequest request,
                                                                                                 final WSFederationRequest fedRequest) {
        final String serviceUrl = constructServiceUrl(request, response, fedRequest);
        final Service targetService = this.serviceSelectionStrategy.resolveServiceFrom(this.webApplicationServiceFactory.createService(serviceUrl));
        final WSFederationRegisteredService svc = getWsFederationRegisteredService(targetService);

        final WsFederationProperties.IdentityProvider idp = casProperties.getAuthn().getWsfedIdp().getIdp();
        if (StringUtils.isBlank(fedRequest.getWtrealm()) || !StringUtils.equals(fedRequest.getWtrealm(), svc.getRealm())) {
            LOGGER.warn("Realm [{}] is not authorized for matching service [{}]", fedRequest.getWtrealm(), svc);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        if (!StringUtils.equals(idp.getRealm(), svc.getRealm())) {
            LOGGER.warn("Realm [{}] is not authorized for the identity provider realm [{}]", fedRequest.getWtrealm(), idp.getRealm());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }

        return svc;
    }

    /**
     * Gets ws federation registered service.
     *
     * @param targetService the target service
     * @return the ws federation registered service
     */
    protected WSFederationRegisteredService getWsFederationRegisteredService(final Service targetService) {
        final WSFederationRegisteredService svc = this.servicesManager.findServiceBy(targetService, WSFederationRegisteredService.class);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(targetService, svc);
        return svc;
    }

    /**
     * Handle unauthorized service exception.
     *
     * @param req the req
     * @param ex  the ex
     * @return the model and view
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnauthorizedServiceException(final HttpServletRequest req, final Exception ex) {
        return WebUtils.produceUnauthorizedErrorView();
    }
}
