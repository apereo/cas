package org.apereo.cas.oidc.web.response;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is {@link OidcResponseModeQueryJwtBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class OidcResponseModeQueryJwtBuilder extends BaseOAuth20JwtResponseModeBuilder {

    public OidcResponseModeQueryJwtBuilder(final ObjectProvider<@NonNull OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    public ModelAndView build(final RegisteredService registeredService,
                              final String redirectUrl, final Map<String, String> parameters) {

        return configurationContext
            .stream()
            .map(ctx -> {
                val token = buildJwtResponse(registeredService, parameters);
                val mv = new RedirectView(redirectUrl);
                return new ModelAndView(mv, Map.of("response", token));
            })
            .findFirst()
            .orElseThrow();
    }

    @Override
    public OAuth20ResponseModeTypes getResponseMode() {
        return OAuth20ResponseModeTypes.QUERY_JWT;
    }
}
