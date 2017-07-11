package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

/**
 * This is {@link OAuth20IdTokenResponseTypeRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20IdTokenResponseTypeRequestValidator extends OAuth20TokenResponseTypeRequestValidator {
    public OAuth20IdTokenResponseTypeRequestValidator(final ServicesManager servicesManager,
                                                      final OAuth20Validator validator) {
        super(servicesManager, validator);
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.IDTOKEN_TOKEN;
    }
}
