package org.apereo.cas.ws.idp.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
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
    public WSFederationValidateRequestController(final WSFederationRequestConfigurationContext ctx) {
        super(ctx);
    }

    /**
     * Handle federation request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = WSFederationConstants.ENDPOINT_FEDERATION_REQUEST)
    public void handleFederationRequest(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
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
                handleInitialAuthenticationRequest(fedRequest, response, request);
                break;
            default:
                throw new UnauthorizedAuthenticationException("The authentication request is not recognized", new HashMap<>(0));
        }
    }

    private void handleLogoutRequest(final WSFederationRequest fedRequest, final HttpServletRequest request,
                                     final HttpServletResponse response) throws Exception {

        val logoutUrl = FunctionUtils.doIf(StringUtils.isNotBlank(fedRequest.getWreply()),
            () -> {
                val service = createService(fedRequest);
                val registeredService = getWsFederationRegisteredService(service);
                LOGGER.debug("Invoking logout operation for request [{}], redirecting next to [{}] matched against [{}]",
                    fedRequest, fedRequest.getWreply(), registeredService);
                val logoutParam = getConfigContext().getCasProperties().getLogout().getRedirectParameter();
                return getConfigContext().getCasProperties()
                    .getServer().getLogoutUrl().concat("?").concat(logoutParam).concat("=").concat(service.getId());
            },
            () -> getConfigContext().getCasProperties().getServer().getLogoutUrl())
            .get();

        val authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, logoutUrl);
    }

    private void handleInitialAuthenticationRequest(final WSFederationRequest fedRequest,
                                                    final HttpServletResponse response,
                                                    final HttpServletRequest request) {

        val targetService = createService(fedRequest);
        val registeredService = findAndValidateFederationRequestForRegisteredService(targetService, fedRequest);
        LOGGER.debug("Redirecting to identity provider for initial authentication [{}]", fedRequest);
        redirectToIdentityProvider(fedRequest, response, request, targetService, registeredService);
    }

    private WebApplicationService createService(final WSFederationRequest fedRequest) {
        val targetService = getConfigContext().getWebApplicationServiceFactory().createService(fedRequest.getWreply());
        targetService.getAttributes().put(WSFederationConstants.WREPLY, CollectionUtils.wrapList(fedRequest.getWreply()));
        targetService.getAttributes().put(WSFederationConstants.WTREALM, CollectionUtils.wrapList(fedRequest.getWtrealm()));
        targetService.getAttributes().put(WSFederationConstants.WCTX, CollectionUtils.wrapList(fedRequest.getWctx()));
        return targetService;
    }

    @SneakyThrows
    private void redirectToIdentityProvider(final WSFederationRequest fedRequest,
                                            final HttpServletResponse response,
                                            final HttpServletRequest request,
                                            final WebApplicationService service,
                                            final WSFederationRegisteredService registeredService) {
        val serviceUrl = constructServiceUrl(request, response, fedRequest);
        LOGGER.debug("Created service url [{}] mapped to [{}]", serviceUrl, registeredService);
        val renew = shouldRenewAuthentication(fedRequest, request);
        var initialUrl = CommonUtils.constructRedirectUrl(getConfigContext().getCasProperties().getServer().getLoginUrl(),
            CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, renew, false);
        val builder = new URIBuilder(initialUrl);
        service.getAttributes().forEach((key, value)
            -> CollectionUtils.firstElement(value).map(Object::toString).ifPresent(v -> builder.addParameter(key, v)));
        initialUrl = builder.toString();
        LOGGER.debug("Redirecting authN request to [{}]", initialUrl);
        val authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, initialUrl);
    }
}
