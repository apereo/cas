package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.oauth.services.OAuth20WebApplicationServiceTests;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20PasswordGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenControllerMemcachedTests;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenControllerTests;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenControllerNoGrantTypeTests;
import org.apereo.cas.support.oauth.web.OAuth20AuthorizeControllerTests;
import org.apereo.cas.support.oauth.web.OAuth20ProfileControllerTests;
import org.apereo.cas.support.oauth.web.audit.AccessTokenGrantRequestAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.audit.OAuth20UserProfileDataAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRendererFlatTests;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRendererNestedTests;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenExpirationPolicyTests;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenSovereignExpirationPolicyTests;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenExpirationPolicyTests;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenSovereignExpirationPolicyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * OAuth test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
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
    OAuth20AccessTokenControllerMemcachedTests.class,
    OAuth20ProfileControllerTests.class,
    OAuth20WebApplicationServiceTests.class,
    OAuth20DefaultUserProfileViewRendererFlatTests.class,
    OAuth20DefaultUserProfileViewRendererNestedTests.class,
    AccessTokenGrantRequestAuditResourceResolverTests.class,
    OAuth20UserProfileDataAuditResourceResolverTests.class
})
@Slf4j
public class OAuthTestSuite {
}
