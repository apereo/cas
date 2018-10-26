package org.apereo.cas;

import org.apereo.cas.support.oauth.authenticator.OAuth20ClientIdClientSecretAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20ProofKeyCodeExchangeAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20UsernamePasswordAuthenticatorTests;
import org.apereo.cas.support.oauth.services.OAuth20WebApplicationServiceTests;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20PasswordGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenControllerNoGrantTypeTests;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenControllerTests;
import org.apereo.cas.support.oauth.web.OAuth20AuthorizeControllerTests;
import org.apereo.cas.support.oauth.web.OAuth20ProfileControllerTests;
import org.apereo.cas.support.oauth.web.OAuth20RefreshTokenTests;
import org.apereo.cas.support.oauth.web.audit.AccessTokenGrantRequestAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.audit.OAuth20UserProfileDataAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRendererFlatTests;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRendererNestedTests;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenExpirationPolicyTests;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenSovereignExpirationPolicyTests;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenExpirationPolicyTests;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenSovereignExpirationPolicyTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * OAuth test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SelectClasses({
    OAuth20AccessTokenControllerTests.class,
    OAuth20AccessTokenControllerNoGrantTypeTests.class,
    OAuth20AuthorizeControllerTests.class,
    OAuthAccessTokenExpirationPolicyTests.class,
    OAuthAccessTokenSovereignExpirationPolicyTests.class,
    OAuthRefreshTokenExpirationPolicyTests.class,
    OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests.class,
    OAuth20PasswordGrantTypeTokenRequestValidatorTests.class,
    OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests.class,
    OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests.class,
    OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests.class,
    OAuthRefreshTokenSovereignExpirationPolicyTests.class,
    OAuth20ProfileControllerTests.class,
    OAuth20WebApplicationServiceTests.class,
    OAuth20UsernamePasswordAuthenticatorTests.class,
    OAuth20ClientIdClientSecretAuthenticatorTests.class,
    OAuth20ProofKeyCodeExchangeAuthenticatorTests.class,
    OAuth20DefaultUserProfileViewRendererFlatTests.class,
    OAuth20DefaultUserProfileViewRendererNestedTests.class,
    AccessTokenGrantRequestAuditResourceResolverTests.class,
    OAuth20UserProfileDataAuditResourceResolverTests.class,
    OAuth20RefreshTokenTests.class
})
public class OAuthTestsSuite {
}
