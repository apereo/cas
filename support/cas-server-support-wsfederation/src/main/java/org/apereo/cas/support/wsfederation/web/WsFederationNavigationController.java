package org.apereo.cas.support.wsfederation.web;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link WsFederationNavigationController}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Controller("wsFederationNavigationController")
@RequestMapping
@Slf4j
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
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;
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
    public View redirectToProvider(final HttpServletRequest request, final HttpServletResponse response) {
        val wsfedId = request.getParameter(PARAMETER_NAME);
        try {
            val cfg = configurations.stream().filter(c -> c.getId().equals(wsfedId)).findFirst().orElse(null);
            if (cfg == null) {
                throw new IllegalArgumentException("Could not locate WsFederation configuration for " + wsfedId);
            }
            val service = determineService(request);
            val id = wsFederationHelper.getRelyingPartyIdentifier(service, cfg);
            val url = cfg.getAuthorizationUrl(id, cfg.getId());
            wsFederationCookieManager.store(request, response, cfg.getId(), service, cfg);
            return new RedirectView(url);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
    }

    private Service determineService(final HttpServletRequest request) {
        val initialService = ObjectUtils.defaultIfNull(argumentExtractor.extractService(request),
            webApplicationServiceFactory.createService(casLoginEndpoint));
        return this.authenticationRequestServiceSelectionStrategies.resolveService(initialService);
    }
}
