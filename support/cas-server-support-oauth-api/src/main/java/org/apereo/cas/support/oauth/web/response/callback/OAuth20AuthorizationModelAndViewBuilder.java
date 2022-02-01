package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * This is {@link OAuth20AuthorizationModelAndViewBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface OAuth20AuthorizationModelAndViewBuilder {
    /**
     * Customize.
     *
     * @param registeredService the registered service
     * @param responseMode      the response mode
     * @param redirectUrl       the redirect url
     * @param parameters        the parameters
     * @return the model and view
     * @throws Exception the exception
     */
    ModelAndView build(OAuthRegisteredService registeredService,
                       OAuth20ResponseModeTypes responseMode,
                       String redirectUrl,
                       Map<String, String> parameters) throws Exception;
}
