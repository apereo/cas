package org.apereo.cas.ws.idp.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;

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
    public void handleFederationRequest(final HttpServletResponse response,
                                              final HttpServletRequest request) throws Exception {
        val fedRequest = WSFederationRequest.of(request);
        val wa = fedRequest.wa();
        if (StringUtils.isBlank(wa)) {
            throw new UnauthorizedAuthenticationException("Unable to determine the [WA] parameter", new HashMap<>());
        }

        switch (wa.toLowerCase(Locale.ENGLISH)) {
            case WSFederationConstants.WSIGNOUT10, WSFederationConstants.WSIGNOUT_CLEANUP10 -> handleLogoutRequest(fedRequest, request, response);
            case WSFederationConstants.WSIGNIN10 -> handleInitialAuthenticationRequest(fedRequest, response, request);
            default -> throw new UnauthorizedAuthenticationException("The authentication request is not recognized", new HashMap<>());
        }
    }

    protected void handleLogoutRequest(final WSFederationRequest fedRequest, final HttpServletRequest request,
                                       final HttpServletResponse response) throws Exception {

        val logoutUrl = FunctionUtils.doIf(StringUtils.isNotBlank(fedRequest.wreply()),
                () -> {
                    val service = createService(fedRequest);
                    val registeredService = getWsFederationRegisteredService(service);
                    LOGGER.debug("Invoking logout operation for request [{}], redirecting next to [{}] matched against [{}]",
                        fedRequest, fedRequest.wreply(), registeredService);
                    val logoutParam = getConfigContext().getCasProperties().getLogout().getRedirectParameter().getFirst();
                    return getConfigContext().getCasProperties().getServer().getLogoutUrl()
                        .concat("?").concat(logoutParam).concat("=").concat(service.getId());
                },
                () -> getConfigContext().getCasProperties().getServer().getLogoutUrl())
            .get();
        response.sendRedirect(logoutUrl);
    }

    private void handleInitialAuthenticationRequest(
        final WSFederationRequest fedRequest,
        final HttpServletResponse response,
        final HttpServletRequest request) throws Exception {

        val targetService = createService(fedRequest);
        val registeredService = findAndValidateFederationRequestForRegisteredService(targetService, fedRequest);
        LOGGER.debug("Redirecting to identity provider for initial authentication [{}]", fedRequest);
        redirectToIdentityProvider(fedRequest, response, request, targetService, registeredService);
    }

    private WebApplicationService createService(final WSFederationRequest fedRequest) {
        val targetService = getConfigContext().getWebApplicationServiceFactory().createService(fedRequest.wreply());
        targetService.getAttributes().put(WSFederationConstants.WREPLY, CollectionUtils.wrapList(fedRequest.wreply()));
        targetService.getAttributes().put(WSFederationConstants.WTREALM, CollectionUtils.wrapList(fedRequest.wtrealm()));
        targetService.getAttributes().put(WSFederationConstants.WCTX, CollectionUtils.wrapList(fedRequest.wctx()));
        return targetService;
    }

    private void redirectToIdentityProvider(
        final WSFederationRequest fedRequest,
        final HttpServletResponse response,
        final HttpServletRequest request,
        final WebApplicationService service,
        final WSFederationRegisteredService registeredService) throws Exception {
        val serviceUrl = constructServiceUrl(request, response, fedRequest);
        LOGGER.debug("Created service url [{}] mapped to [{}]", serviceUrl, registeredService);
        val renew = shouldRenewAuthentication(fedRequest, request);
        var initialUrl = constructRedirectUrl(
            getConfigContext().getCasProperties().getServer().getLoginUrl(), serviceUrl, renew);
        val builder = new URIBuilder(initialUrl);
        service.getAttributes().forEach((key, value)
            -> CollectionUtils.firstElement(value).map(Object::toString).ifPresent(v -> builder.addParameter(key, v)));
        initialUrl = builder.toString();
        LOGGER.debug("Redirecting authN request to [{}]", initialUrl);
        response.sendRedirect(initialUrl);
    }

    private static String constructRedirectUrl(final String casServerLoginUrl,
                                               final String serviceUrl, final boolean renew) {
        return casServerLoginUrl + '?' + CasProtocolConstants.PARAMETER_SERVICE + '='
               + EncodingUtils.urlEncode(serviceUrl)
               + (renew ? '&' + CasProtocolConstants.PARAMETER_RENEW + "=true" : StringUtils.EMPTY);
    }
}
