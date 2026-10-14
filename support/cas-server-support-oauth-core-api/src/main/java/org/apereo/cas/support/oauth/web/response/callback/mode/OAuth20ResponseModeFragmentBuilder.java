package org.apereo.cas.support.oauth.web.response.callback.mode;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This is {@link OAuth20ResponseModeFragmentBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class OAuth20ResponseModeFragmentBuilder implements OAuth20ResponseModeBuilder {

    @Override
    public OAuth20ResponseModeTypes getResponseMode() {
        return OAuth20ResponseModeTypes.FRAGMENT;
    }

    @Override
    public ModelAndView build(final RegisteredService registeredService, final String redirectUrl,
                              final Map<String, String> parameters)
            throws Exception {

        val fragment = parameters.entrySet()
            .stream()
            .map(entry -> entry.getKey() + '=' + entry.getValue())
            .collect(Collectors.joining("&"));

        /*
         * The redirect URI keeps its own query, minus any parameter the response carries in the fragment. The query is
         * replaced rather than added to, which used to repeat every parameter of the redirect URI, and its values are
         * kept as they were encoded in the redirect URI.
         */
        val urlBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        val queryParams = new LinkedMultiValueMap<String, @NonNull String>();
        urlBuilder.build().getQueryParams().forEach((name, values) -> {
            if (!parameters.containsKey(name)) {
                queryParams.put(name, values);
            }
        });

        val resultUrl = urlBuilder
            .replaceQueryParams(queryParams)
            .fragment(fragment)
            .build()
            .toUriString();
        
        LOGGER.debug("Redirecting to [{}]", resultUrl);
        val mv = new RedirectView(resultUrl);
        return new ModelAndView(mv);
    }
}
