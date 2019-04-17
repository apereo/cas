package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;

import org.junit.jupiter.api.Tag;

/**
 * This is {@link OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
public class OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests extends OAuth20PasswordGrantTypeTokenRequestValidatorTests {
    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.CLIENT_CREDENTIALS;
    }

    @Override
    protected OAuth20GrantTypes getWrongGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }
}
