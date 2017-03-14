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
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWSFederationRequestController.class);

    /**
     * Idp config service.
     */
    protected final IdentityProviderConfigurationService identityProviderConfigurationService;

    protected final ServicesManager servicesManager;

    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    protected final Service callbackService;

    protected final CasConfigurationProperties casProperties;

    protected final AuthenticationServiceSelectionStrategy serviceSelectionStrategy;

    protected final HttpClient httpClient;

    protected final SecurityTokenTicketFactory securityTokenTicketFactory;

    protected final TicketRegistry ticketRegistry;

    protected final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    protected final TicketRegistrySupport ticketRegistrySupport;

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

    protected String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                         final WSFederationRequest WSFederationRequest) {
        try {
            final URIBuilder builder = new URIBuilder(this.callbackService.getId());

            builder.addParameter(WSFederationConstants.WA, WSFederationRequest.getWa());
            builder.addParameter(WSFederationConstants.WREPLY, WSFederationRequest.getWreply());
            builder.addParameter(WSFederationConstants.WTREALM, WSFederationRequest.getWtrealm());

            if (StringUtils.isNotBlank(WSFederationRequest.getWctx())) {
                builder.addParameter(WSFederationConstants.WCTX, WSFederationRequest.getWctx());
            }
            if (StringUtils.isNotBlank(WSFederationRequest.getWfresh())) {
                builder.addParameter(WSFederationConstants.WREFRESH, WSFederationRequest.getWfresh());
            }
            if (StringUtils.isNotBlank(WSFederationRequest.getWhr())) {
                builder.addParameter(WSFederationConstants.WHR, WSFederationRequest.getWhr());
            }
            if (StringUtils.isNotBlank(WSFederationRequest.getWreq())) {
                builder.addParameter(WSFederationConstants.WREQ, WSFederationRequest.getWreq());
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

    protected boolean shouldRedirectForAuthentication(final WSFederationRequest fedRequest,
                                                      final HttpServletResponse response,
                                                      final HttpServletRequest request) {
        return isSecurityTokenExpired(fedRequest, response, request)
                || isAuthenticationRequired(fedRequest, response, request);
    }
    
    protected boolean isSecurityTokenExpired(final WSFederationRequest fedRequest,
                                           final HttpServletResponse response,
                                           final HttpServletRequest request) {
        final SecurityToken idpToken = getSecurityTokenFromRequest(request);
        if (idpToken == null) {
            return true;
        }

        return idpToken.isExpired();
    }

    protected SecurityToken getSecurityTokenFromRequest(final HttpServletRequest request) {
        final String cookieValue = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isNotBlank(cookieValue)) {
            final String sts = securityTokenTicketFactory.getId(cookieValue);
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
