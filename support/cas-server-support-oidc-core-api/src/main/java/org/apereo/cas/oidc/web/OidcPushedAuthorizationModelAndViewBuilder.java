package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;

import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OidcPushedAuthorizationModelAndViewBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class OidcPushedAuthorizationModelAndViewBuilder implements OAuth20AuthorizationModelAndViewBuilder {
    @Override
    public ModelAndView build(final OAuthRegisteredService registeredService,
                              final OAuth20ResponseModeTypes responseMode,
                              final String redirectUrl,
                              final Map<String, String> parameters) {
        val model = new LinkedHashMap<String, Object>();
        model.put(OidcConstants.EXPIRES_IN, Long.valueOf(parameters.get(OidcConstants.EXPIRES_IN)));
        model.put(OidcConstants.REQUEST_URI, parameters.get(OidcConstants.REQUEST_URI));
        val mv = new ModelAndView(new MappingJackson2JsonView(), model);
        mv.setStatus(HttpStatus.CREATED);
        return mv;
    }
}
