package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;

import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * This is {@link OAuth20ResponseModeBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface OAuth20ResponseModeBuilder {

    /**
     * Build model and view.
     *
     * @param registeredService the registered service
     * @param redirectUrl       the redirect url
     * @param parameters        the parameters
     * @return the model and view
     * @throws Exception the exception
     */
    ModelAndView build(RegisteredService registeredService, String redirectUrl,
                       Map<String, String> parameters) throws Exception;

    /**
     * Gets response mode.
     *
     * @return the response mode
     */
    OAuth20ResponseModeTypes getResponseMode();
}
