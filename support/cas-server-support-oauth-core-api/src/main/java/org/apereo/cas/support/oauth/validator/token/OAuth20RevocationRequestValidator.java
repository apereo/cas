package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20RevocationRequestValidator}.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class OAuth20RevocationRequestValidator implements OAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;

    private final SessionStore sessionStore;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean validate(final WebContext context) {
        val clientId = OAuth20Utils.getClientIdAndClientSecret(context, sessionStore).getLeft();
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);

        if (registeredService == null) {
            LOGGER.warn("Provided client id [{}] cannot be matched against a service definition", clientId);
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(final WebContext context) {
        val token = OAuth20Utils.getRequestParameter(context, OAuth20Constants.TOKEN)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isBlank(token)) {
            return false;
        }

        val clientId = OAuth20Utils.getClientIdAndClientSecret(context, sessionStore).getLeft();
        return StringUtils.isNotBlank(clientId);
    }
}
