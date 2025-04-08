package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.services.SamlIdentityProviderDiscoveryFeedService;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;


/**
 * This is {@link SamlIdentityProviderDiscoveryFeedController}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RestController("identityProviderDiscoveryFeedController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = SamlIdentityProviderDiscoveryFeedController.BASE_ENDPOINT_IDP_DISCOVERY)
public class SamlIdentityProviderDiscoveryFeedController {
    /**
     * Base endpoint url.
     */
    public static final String BASE_ENDPOINT_IDP_DISCOVERY = "/idp/discovery";

    private final CasConfigurationProperties casProperties;

    private final SamlIdentityProviderDiscoveryFeedService samlIdentityProviderDiscoveryFeedService;


    /**
     * Gets discovery feed.
     *
     * @param request  the request
     * @param response the response
     * @param entityID the entity id
     * @return the discovery feed
     */
    @GetMapping(path = "/feed", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<SamlIdentityProviderEntity> getDiscoveryFeed(
        final HttpServletRequest request,
        final HttpServletResponse response,
        @RequestParam(value = "entityID", required = false)
        final String entityID) {
        val feed = samlIdentityProviderDiscoveryFeedService.getDiscoveryFeed(request, response);
        if (StringUtils.hasText(entityID)) {
            return feed
                .stream()
                .filter(idp -> RegexUtils.find(entityID, idp.getEntityID()))
                .toList();
        }
        return feed;
    }

    /**
     * Home.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    public ModelAndView home(final HttpServletRequest request, final HttpServletResponse response) {
        val model = new HashMap<String, Object>();

        val entityIds = samlIdentityProviderDiscoveryFeedService.getEntityIds(request, response);

        LOGGER.debug("Using identity provider entity ids [{}]", entityIds);
        model.put("entityIds", entityIds);

        model.put("casServerPrefix", casProperties.getServer().getPrefix());

        model.put("httpRequestSecure", request.isSecure());
        model.put("httpRequestMethod", request.getMethod());
        model.put("httpRequestHeaders", HttpRequestUtils.getRequestHeaders(request));

        return new ModelAndView("saml2-discovery/casSamlIdPDiscoveryView", model);
    }

    /**
     * Redirect.
     *
     * @param entityID            the entity id
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @return the view
     */
    @GetMapping(path = "/redirect")
    public View redirect(
        @RequestParam("entityID")
        final String entityID,
        final HttpServletRequest httpServletRequest,
        final HttpServletResponse httpServletResponse) throws Throwable {
        val provider = samlIdentityProviderDiscoveryFeedService.getProvider(entityID, httpServletRequest, httpServletResponse);
        return new RedirectView('/' + provider.getRedirectUrl(), true, true, true);
    }
}
