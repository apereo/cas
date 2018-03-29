package org.apereo.cas.support.oauth.validator.authorization;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;

/**
 * This is {@link OAuth20TokenResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OAuth20TokenResponseTypeAuthorizationRequestValidator extends OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator {
    public OAuth20TokenResponseTypeAuthorizationRequestValidator(final ServicesManager servicesManager,
                                                                 final OAuth20Validator validator) {
        super(servicesManager, validator);
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.TOKEN;
    }
}
