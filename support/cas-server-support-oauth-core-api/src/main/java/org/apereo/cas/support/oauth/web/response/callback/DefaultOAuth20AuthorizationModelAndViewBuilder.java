package org.apereo.cas.support.oauth.web.response.callback;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link DefaultOAuth20AuthorizationModelAndViewBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultOAuth20AuthorizationModelAndViewBuilder implements OAuth20AuthorizationModelAndViewBuilder {
    private final OAuth20ResponseModeFactory responseModeFactory;

    @Override
    public ModelAndView build(final OAuthRegisteredService registeredService,
                              final OAuth20ResponseModeTypes responseMode,
                              final String url, final Map<String, String> parameters) throws Exception {
        val redirectUrl = prepareRedirectUrl(registeredService, url, parameters);
        val builder = responseModeFactory.getBuilder(registeredService, responseMode);
        return builder.build(registeredService, redirectUrl, parameters);
    }

    protected String prepareRedirectUrl(final OAuthRegisteredService registeredService,
                                        final String redirectUrl, final Map<String, String> parameters) throws Exception {
        return redirectUrl;
    }
}
