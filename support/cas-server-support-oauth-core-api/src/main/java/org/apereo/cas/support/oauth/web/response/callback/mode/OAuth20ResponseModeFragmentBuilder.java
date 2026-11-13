package org.apereo.cas.support.oauth.web.response.callback.mode;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.jspecify.annotations.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

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

    /**
     * Build the redirect that carries the response parameters in the fragment of the redirect URI.
     * Each parameter name and value is percent-encoded as a URI component, so values such as a JSON {@code state}
     * reach the client intact (RFC 6749, section 4.2.2 and appendix B) instead of breaking the fragment or the
     * redirect. The URL is complete once built here, so the redirect view does not expand URI template variables.
     *
     * @param registeredService the registered service
     * @param redirectUrl       the redirect url
     * @param parameters        the response parameters
     * @return the model and view
     * @throws Exception the exception
     */
    @Override
    public ModelAndView build(final RegisteredService registeredService, final String redirectUrl,
                              final Map<String, String> parameters) throws Exception {

        val urlBuilder = new URIBuilder(redirectUrl);
        val currentParams = urlBuilder.getQueryParams();
        urlBuilder.removeQuery();
        currentParams.removeIf(p -> parameters.containsKey(p.getName()));
        val fragment = parameters.entrySet()
            .stream()
            .map(entry -> UriUtils.encode(entry.getKey(), StandardCharsets.UTF_8)
                + '=' + UriUtils.encode(entry.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));

        val queryParams = new LinkedMultiValueMap<@NonNull String, @NonNull String>(currentParams.size());
        currentParams.forEach(param -> queryParams.put(param.getName(), List.of(param.getValue())));
        
        val resultUrl = UriComponentsBuilder.fromUriString(redirectUrl)
            .fragment(fragment)
            .queryParams(queryParams)
            .build()
            .toUriString();
        
        LOGGER.debug("Redirecting to [{}]", resultUrl);
        val mv = new RedirectView(resultUrl);
        mv.setExpandUriTemplateVariables(false);
        return new ModelAndView(mv);
    }
}
