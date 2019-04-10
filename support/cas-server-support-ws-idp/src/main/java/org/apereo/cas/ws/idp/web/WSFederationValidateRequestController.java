package org.apereo.cas.ws.idp.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.util.CommonUtils;
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
@Slf4j
public class WSFederationValidateRequestController extends BaseWSFederationRequestController {
    public WSFederationValidateRequestController(final WSFederationRequestConfigurationContext wsFederationRequestConfigurationContext) {
        super(wsFederationRequestConfigurationContext);
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
        val fedRequest = WSFederationRequest.of(request);
        val wa = fedRequest.getWa();
        if (StringUtils.isBlank(wa)) {
            throw new UnauthorizedAuthenticationException("Unable to determine the [WA] parameter", new HashMap<>(0));
        }

        switch (wa.toLowerCase()) {
            case WSFederationConstants.WSIGNOUT10:
            case WSFederationConstants.WSIGNOUT_CLEANUP10:
                handleLogoutRequest(fedRequest, request, response);
                break;
            case WSFederationConstants.WSIGNIN10:
                val targetService = getWsFederationRequestConfigurationContext().getWebApplicationServiceFactory().createService(fedRequest.getWreply());
                handleInitialAuthenticationRequest(fedRequest, targetService, response, request);
                break;
            default:
                throw new UnauthorizedAuthenticationException("The authentication request is not recognized", new HashMap<>(0));
        }
    }

    private void handleLogoutRequest(final WSFederationRequest fedRequest, final HttpServletRequest request,
                                     final HttpServletResponse response) throws Exception {

        val logoutUrl = FunctionUtils.doIf(StringUtils.isNotBlank(fedRequest.getWreply()),
            () -> {
                val service = getWsFederationRequestConfigurationContext().getWebApplicationServiceFactory().createService(fedRequest.getWreply());
                val registeredService = getWsFederationRegisteredService(service);
                LOGGER.debug("Invoking logout operation for request [{}], redirecting next to [{}] matched against [{}]",
                    fedRequest, fedRequest.getWreply(), registeredService);
                val logoutParam = getWsFederationRequestConfigurationContext().getCasProperties().getLogout().getRedirectParameter();
                return getWsFederationRequestConfigurationContext().getCasProperties()
                    .getServer().getLogoutUrl().concat("?").concat(logoutParam).concat("=").concat(service.getId());
            },
            () -> getWsFederationRequestConfigurationContext().getCasProperties().getServer().getLogoutUrl())
            .get();

        val authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, logoutUrl);
    }

    private void handleInitialAuthenticationRequest(final WSFederationRequest fedRequest, final Service targetService,
                                                    final HttpServletResponse response, final HttpServletRequest request) {
        val service = findAndValidateFederationRequestForRegisteredService(targetService, fedRequest);
        LOGGER.debug("Redirecting to identity provider for initial authentication [{}]", fedRequest);
        redirectToIdentityProvider(fedRequest, response, request, service);
    }

    @SneakyThrows
    private void redirectToIdentityProvider(final WSFederationRequest fedRequest, final HttpServletResponse response,
                                            final HttpServletRequest request, final WSFederationRegisteredService service) {
        val serviceUrl = constructServiceUrl(request, response, fedRequest);
        LOGGER.debug("Created service url [{}] mapped to [{}]", serviceUrl, service);
        val renew = shouldRenewAuthentication(fedRequest, request);
        val initialUrl = CommonUtils.constructRedirectUrl(getWsFederationRequestConfigurationContext().getCasProperties().getServer().getLoginUrl(),
            CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, renew, false);
        LOGGER.debug("Redirecting authN request to [{}]", initialUrl);
        val authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, initialUrl);
    }
}
