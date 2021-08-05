package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * This is {@link BaseOAuth20AuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BaseOAuth20AuthorizationResponseBuilder implements OAuth20AuthorizationResponseBuilder {
   
    @Override
    public ModelAndView buildResponseModelAndView(final JEEContext context, final ServicesManager servicesManager,
                                                  final String clientId, final String redirectUrl,
                                                  final Map<String, String> parameters) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        return OAuth20Utils.buildResponseModelAndView(context, registeredService, redirectUrl, parameters);
    }
}
