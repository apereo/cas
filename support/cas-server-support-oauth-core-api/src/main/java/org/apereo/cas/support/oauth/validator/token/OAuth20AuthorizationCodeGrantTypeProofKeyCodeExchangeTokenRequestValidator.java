package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator
    extends OAuth20AuthorizationCodeGrantTypeTokenRequestValidator {
    public OAuth20AuthorizationCodeGrantTypeProofKeyCodeExchangeTokenRequestValidator(
        final ObjectProvider<@NonNull OAuth20ConfigurationContext> configurationContext) {
        super(configurationContext);
    }
}
