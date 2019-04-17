package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator extends OAuth20AuthorizationCodeGrantTypeTokenRequestValidator {
    public OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }
}
