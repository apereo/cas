package org.apereo.cas.oidc.web.response;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This is {@link OidcResponseModeFragmentJwtBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class OidcResponseModeFragmentJwtBuilder extends BaseOAuth20JwtResponseModeBuilder {

    public OidcResponseModeFragmentJwtBuilder(final ObjectProvider<@NonNull OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    public ModelAndView build(final RegisteredService registeredService,
                              final String redirectUrl,
                              final Map<String, String> parameters) {

        return configurationContext
            .stream()
            .map(Unchecked.function(ctx -> {
                val token = buildJwtResponse(registeredService, parameters);
                val url = UriComponentsBuilder.fromUriString(redirectUrl).fragment("response=" + token).build().toUriString();
                LOGGER.debug("Redirecting to [{}]", url);
                return new ModelAndView(new RedirectView(url));
            }))
            .findFirst()
            .orElseThrow();
    }

    @Override
    public OAuth20ResponseModeTypes getResponseMode() {
        return OAuth20ResponseModeTypes.FRAGMENT_JWT;
    }
}
