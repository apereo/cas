package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import lombok.RequiredArgsConstructor;
import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * This is {@link BaseOAuth20AuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public abstract class BaseOAuth20AuthorizationResponseBuilder implements OAuth20AuthorizationResponseBuilder {

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * CAS configuration properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * Response customizer.
     */
    protected final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder;

    @Override
    public ModelAndView build(final WebContext context,
                              final OAuthRegisteredService registeredService,
                              final String redirectUrl,
                              final Map<String, String> parameters) {
        return authorizationModelAndViewBuilder.build(context, registeredService, redirectUrl, parameters);
    }
}
