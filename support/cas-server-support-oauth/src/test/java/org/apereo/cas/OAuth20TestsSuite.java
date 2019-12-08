package org.apereo.cas;

import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientIdClientSecretAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20ProofKeyCodeExchangeAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20UsernamePasswordAuthenticatorTests;
import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutorTests;
import org.apereo.cas.support.oauth.services.OAuth20WebApplicationServiceTests;
import org.apereo.cas.support.oauth.util.OAuth20UtilsTests;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20PasswordGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.web.OAuth20RefreshTokenTests;
import org.apereo.cas.support.oauth.web.audit.OAuth20AccessTokenGrantRequestAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.audit.OAuth20UserProfileDataAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20IntrospectionEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointControllerTests;
import org.apereo.cas.support.oauth.web.mgmt.OAuth20TokenManagementEndpointTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenAtHashGeneratorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultAccessTokenResponseGeneratorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGeneratorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20JwtAccessTokenEncoderTests;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRendererFlatTests;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRendererNestedTests;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenExpirationPolicyTests;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenSovereignExpirationPolicyTests;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenExpirationPolicyTests;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenSovereignExpirationPolicyTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * OAuth test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SelectClasses({
    OAuth20AccessTokenEndpointControllerTests.class,
    OAuth20AuthorizeEndpointControllerTests.class,
    OAuth20AccessTokenExpirationPolicyTests.class,
    OAuth20AccessTokenSovereignExpirationPolicyTests.class,
    OAuth20RefreshTokenExpirationPolicyTests.class,
    OAuth20DefaultTokenGeneratorTests.class,
    OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests.class,
    OAuth20PasswordGrantTypeTokenRequestValidatorTests.class,
    OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests.class,
    OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests.class,
    OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests.class,
    OAuth20RefreshTokenSovereignExpirationPolicyTests.class,
    OAuth20UserProfileEndpointControllerTests.class,
    OAuth20UtilsTests.class,
    OAuth20AccessTokenAtHashGeneratorTests.class,
    OAuth20IntrospectionEndpointControllerTests.class,
    OAuth20TokenManagementEndpointTests.class,
    OAuth20JwtAccessTokenEncoderTests.class,
    OAuth20WebApplicationServiceTests.class,
    OAuth20UsernamePasswordAuthenticatorTests.class,
    OAuth20AccessTokenAuthenticatorTests.class,
    OAuth20ClientIdClientSecretAuthenticatorTests.class,
    OAuth20ProofKeyCodeExchangeAuthenticatorTests.class,
    OAuth20DefaultUserProfileViewRendererFlatTests.class,
    OAuth20DefaultUserProfileViewRendererNestedTests.class,
    OAuth20AccessTokenGrantRequestAuditResourceResolverTests.class,
    OAuth20UserProfileDataAuditResourceResolverTests.class,
    OAuth20DefaultAccessTokenResponseGeneratorTests.class,
    OAuth20RefreshTokenTests.class,
    OAuth20RegisteredServiceCipherExecutorTests.class
})
@RunWith(JUnitPlatform.class)
public class OAuth20TestsSuite {
}
