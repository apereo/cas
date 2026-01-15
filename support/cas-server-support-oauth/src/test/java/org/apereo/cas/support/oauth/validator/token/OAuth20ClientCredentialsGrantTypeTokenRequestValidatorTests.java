package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
@TestPropertySource(properties = "cas.authn.oauth.session-replication.replicate-sessions=false")
class OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests extends OAuth20PasswordGrantTypeTokenRequestValidatorTests {
    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.CLIENT_CREDENTIALS;
    }

    @Override
    protected OAuth20GrantTypes getWrongGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }
}
