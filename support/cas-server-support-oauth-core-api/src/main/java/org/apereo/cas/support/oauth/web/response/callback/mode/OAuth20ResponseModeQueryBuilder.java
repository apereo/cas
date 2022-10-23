package org.apereo.cas.support.oauth.web.response.callback.mode;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;

import lombok.val;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * This is {@link OAuth20ResponseModeQueryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OAuth20ResponseModeQueryBuilder implements OAuth20ResponseModeBuilder {
    @Override
    public ModelAndView build(final RegisteredService registeredService,
                              final String redirectUrl, final Map<String, String> parameters) throws Exception {
        val mv = new RedirectView(redirectUrl);
        return new ModelAndView(mv, parameters);
    }
}
