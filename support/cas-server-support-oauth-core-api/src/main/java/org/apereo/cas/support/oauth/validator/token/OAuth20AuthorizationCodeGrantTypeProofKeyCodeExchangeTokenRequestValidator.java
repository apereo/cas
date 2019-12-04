package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator extends OAuth20AuthorizationCodeGrantTypeTokenRequestValidator {
    public OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }
}
