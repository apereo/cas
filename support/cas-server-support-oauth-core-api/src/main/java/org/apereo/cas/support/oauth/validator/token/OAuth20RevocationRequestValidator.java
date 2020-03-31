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
import org.pac4j.core.context.JEEContext;
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

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean validate(final JEEContext context) {
        val clientId = OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);

        if (registeredService == null) {
            LOGGER.warn("Provided client id [{}] cannot be matched against a service definition", clientId);
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(final JEEContext context) {
        val token = context.getRequestParameter(OAuth20Constants.TOKEN)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isBlank(token)) {
            return false;
        }

        val clientId = OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
        return StringUtils.isNotBlank(clientId);
    }
}
