package org.apereo.cas.ws.idp.web;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * This is {@link WSFederationValidateRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class WSFederationValidateRequestController extends BaseWSFederationRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WSFederationValidateRequestController.class);

    public WSFederationValidateRequestController(
            final ServicesManager servicesManager,
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final CasConfigurationProperties casProperties,
            final AuthenticationServiceSelectionStrategy serviceSelectionStrategy,
            final HttpClient httpClient,
            final SecurityTokenTicketFactory securityTokenTicketFactory,
            final TicketRegistry ticketRegistry,
            final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
            final TicketRegistrySupport ticketRegistrySupport) {
        super(servicesManager,
                webApplicationServiceFactory, casProperties, serviceSelectionStrategy, httpClient,
                securityTokenTicketFactory, ticketRegistry, ticketGrantingTicketCookieGenerator, ticketRegistrySupport);
    }

    /**
     * Handle federation request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = WSFederationConstants.ENDPOINT_FEDERATION_REQUEST)
    protected void handleFederationRequest(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        final WSFederationRequest fedRequest = WSFederationRequest.of(request);
        switch (fedRequest.getWa().toLowerCase()) {
            case WSFederationConstants.WSIGNOUT10:
            case WSFederationConstants.WSIGNOUT_CLEANUP10:
                handleLogoutRequest(fedRequest, request, response);
                break;
            case WSFederationConstants.WSIGNIN10:
                handleInitialAuthenticationRequest(fedRequest, response, request);
                break;
            default:
                throw new UnauthorizedAuthenticationException("The authentication request is not recognized",
                        new HashMap<>(0));
        }
    }

    private void handleLogoutRequest(final WSFederationRequest fedRequest, final HttpServletRequest request,
                                     final HttpServletResponse response) throws Exception {
        String logoutUrl = casProperties.getServer().getLogoutUrl();
        if (StringUtils.isNotBlank(fedRequest.getWreply())) {
            final Service service = webApplicationServiceFactory.createService(fedRequest.getWreply());
            final WSFederationRegisteredService registeredService = getWsFederationRegisteredService(service);
            LOGGER.debug("Invoking logout operation for request [{}], redirecting next to [{}] matched against [{}]",
                    fedRequest, fedRequest.getWreply(), registeredService);
            final String logoutParam = casProperties.getLogout().getRedirectParameter();
            logoutUrl = logoutUrl.concat("?").concat(logoutParam).concat("=").concat(service.getId());
        }
        final AuthenticationRedirectStrategy authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, logoutUrl);
    }

    private void handleInitialAuthenticationRequest(final WSFederationRequest fedRequest,
                                                    final HttpServletResponse response, final HttpServletRequest request) {
        final WSFederationRegisteredService service = findAndValidateFederationRequestForRegisteredService(response, request, fedRequest);
        LOGGER.debug("Redirecting to identity provider for initial authentication [{}]", fedRequest);
        redirectToIdentityProvider(fedRequest, response, request, service);
    }

    private void redirectToIdentityProvider(final WSFederationRequest fedRequest, final HttpServletResponse response,
                                            final HttpServletRequest request, final WSFederationRegisteredService service) {
        try {
            final String serviceUrl = constructServiceUrl(request, response, fedRequest);
            LOGGER.debug("Created service url [{}] mapped to [{}]", serviceUrl, service);
            final boolean renew = shouldRenewAuthentication(fedRequest, request);
            final String initialUrl = CommonUtils.constructRedirectUrl(casProperties.getServer().getLoginUrl(),
                    CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, renew, false);
            LOGGER.debug("Redirecting authN request to [{}]", initialUrl);
            final AuthenticationRedirectStrategy authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
            authenticationRedirectStrategy.redirect(request, response, initialUrl);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
