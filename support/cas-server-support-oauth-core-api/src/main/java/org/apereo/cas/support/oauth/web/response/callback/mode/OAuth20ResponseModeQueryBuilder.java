package org.apereo.cas.support.oauth.web.response.callback.mode;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is {@link OAuth20ResponseModeQueryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class OAuth20ResponseModeQueryBuilder implements OAuth20ResponseModeBuilder {
    @Override
    public ModelAndView build(final RegisteredService registeredService,
                              final String redirectUrl,
                              final Map<String, String> parameters) {
        val mv = new RedirectView(redirectUrl);
        return new ModelAndView(mv, parameters);
    }

    @Override
    public OAuth20ResponseModeTypes getResponseMode() {
        return OAuth20ResponseModeTypes.QUERY;
    }
}
