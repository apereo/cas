package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BaseOAuth20AuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public abstract class BaseOAuth20AuthorizationResponseBuilder<T extends OAuth20ConfigurationContext>
    implements OAuth20AuthorizationResponseBuilder {

    /**
     * Configuration context.
     */
    protected final T configurationContext;

    /**
     * Response customizer.
     */
    protected final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder;

    @Override
    public ModelAndView build(final OAuthRegisteredService registeredService,
                              final OAuth20ResponseModeTypes responseMode,
                              final String redirectUrl,
                              final Map<String, String> parameters) throws Exception {
        return authorizationModelAndViewBuilder.build(registeredService, responseMode, redirectUrl, parameters);
    }

    @Override
    public Optional<OAuth20AuthorizationRequest.OAuth20AuthorizationRequestBuilder> toAuthorizationRequest(
        final WebContext context, final Authentication authentication,
        final Service service, final OAuthRegisteredService registeredService) {
        return Optional.of(OAuth20AuthorizationRequest.builder()
            .clientId(OAuth20Utils.getRequestParameter(context, OAuth20Constants.CLIENT_ID).map(String::valueOf).orElse(StringUtils.EMPTY))
            .url(context.getRequestURL())
            .responseType(OAuth20Utils.getRequestParameter(context, OAuth20Constants.RESPONSE_TYPE).map(String::valueOf).orElse(StringUtils.EMPTY))
            .grantType(OAuth20Utils.getRequestParameter(context, OAuth20Constants.GRANT_TYPE).map(String::valueOf).orElse(StringUtils.EMPTY)));
    }
}
