package org.apereo.cas.support.wsfederation.web;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.web.support.ArgumentExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link WsFederationNavigationController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Controller("wsFederationNavigationController")
@RequestMapping
@Tag(name = "WS Federation")
@RequiredArgsConstructor
public class WsFederationNavigationController {
    /**
     * Endpoint path controlled by this controller to make the redirect.
     */
    public static final String ENDPOINT_REDIRECT = "/wsfedredirect";

    /**
     * The parameter name that indicates the wsfederation client id.
     */
    public static final String PARAMETER_NAME = "wsfedclientid";

    private final WsFederationCookieManager wsFederationCookieManager;

    private final WsFederationHelper wsFederationHelper;

    private final Collection<WsFederationConfiguration> configurations;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final String casLoginEndpoint;

    private final ArgumentExtractor argumentExtractor;

    /**
     * Redirect to provider. Receive the client name from the request and then try to determine and build the endpoint url
     * for the redirection. The redirection data/url must contain a delegated client ticket id so that the request be can
     * restored on the trip back. SAML clients use the relay-state session attribute while others use request parameters.
     *
     * @param request  the request
     * @param response the response
     * @return the view
     */
    @GetMapping(ENDPOINT_REDIRECT)
    @Operation(summary = "Redirect to WS-Federation provider",
        parameters = @Parameter(name = PARAMETER_NAME, in = ParameterIn.QUERY, required = true, description = "WS-Federation client id"))
    public View redirectToProvider(
        @RequestParam(value = PARAMETER_NAME, required = true) final String wsfedId,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        val cfg = configurations.stream().filter(configuration -> configuration.getId().equals(wsfedId)).findFirst()
            .orElseThrow(() -> UnauthorizedServiceException.denied("Could not locate WsFederation configuration for %s".formatted(wsfedId)));
        val service = determineService(request);
        val id = wsFederationHelper.getRelyingPartyIdentifier(service, cfg);
        val url = cfg.getAuthorizationUrl(id, cfg.getId());
        wsFederationCookieManager.store(request, response, cfg.getId(), service, cfg);
        return new RedirectView(url);
    }

    protected Service determineService(final HttpServletRequest request) {
        request.setAttribute(ServiceFactory.COLLECT_SERVICE_ATTRIBUTES, false);
        val defaultService = webApplicationServiceFactory.createService(casLoginEndpoint);
        return ObjectUtils.defaultIfNull(argumentExtractor.extractService(request), defaultService);
    }
}
