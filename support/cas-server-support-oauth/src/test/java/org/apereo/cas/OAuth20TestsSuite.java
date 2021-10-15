package org.apereo.cas;

import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientIdClientSecretAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20DefaultCasAuthenticationBuilderTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20ProofKeyCodeExchangeAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20RefreshTokenAuthenticatorTests;
import org.apereo.cas.support.oauth.authenticator.OAuth20UsernamePasswordAuthenticatorTests;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilterTests;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfileDataCreatorTests;
import org.apereo.cas.support.oauth.profile.OAuth20ClientIdAwareProfileManagerTests;
import org.apereo.cas.support.oauth.services.OAuth20AuthenticationServiceSelectionStrategyTests;
import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutorTests;
import org.apereo.cas.support.oauth.services.OAuth20ServicesManagerRegisteredServiceLocatorTests;
import org.apereo.cas.support.oauth.services.OAuth20WebApplicationServiceTests;
import org.apereo.cas.support.oauth.util.OAuth20UtilsTests;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20IdTokenResponseTypeAuthorizationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20TokenResponseTypeAuthorizationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20DeviceCodeResponseTypeRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20PasswordGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.OAuth20RevocationRequestValidatorTests;
import org.apereo.cas.support.oauth.validator.token.device.InvalidOAuth20DeviceTokenExceptionTests;
import org.apereo.cas.support.oauth.validator.token.device.UnapprovedOAuth20DeviceUserCodeExceptionTests;
import org.apereo.cas.support.oauth.web.CSRFCookieTests;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenSecurityLogicTests;
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolverTests;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapterTests;
import org.apereo.cas.support.oauth.web.OAuth20RefreshTokenTests;
import org.apereo.cas.support.oauth.web.audit.OAuth20AccessTokenGrantRequestAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.audit.OAuth20AccessTokenResponseAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.audit.OAuth20CodeResponseAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.audit.OAuth20UserProfileDataAuditResourceResolverTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20CallbackAuthorizeEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20DeviceUserCodeApprovalEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20IntrospectionEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20RevocationEndpointControllerTests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointControllerTests;
import org.apereo.cas.support.oauth.web.mgmt.OAuth20TokenManagementEndpointTests;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilderTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenAtHashGeneratorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultAccessTokenResponseGeneratorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGeneratorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20JwtAccessTokenEncoderTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResultTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenAuthorizationCodeGrantRequestExtractorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenPasswordGrantRequestExtractorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRefreshTokenGrantRequestExtractorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenCipherExecutorTests;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutorTests;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilderTests;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20InvalidAuthorizationResponseBuilderTests;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResourceOwnerCredentialsResponseBuilderTests;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilderTests;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRendererFlatTests;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRendererNestedTests;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionServiceTests;
import org.apereo.cas.ticket.TokenSigningAndEncryptionServiceTests;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenExpirationPolicyTests;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenSovereignExpirationPolicyTests;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessTokenFactoryTests;
import org.apereo.cas.ticket.accesstoken.OAuth20JwtBuilderTests;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactoryTests;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceTokenFactoryTests;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceUserCodeTests;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenExpirationPolicyBuilderTests;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenUtilsTests;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshTokenFactoryTests;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenExpirationPolicyTests;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenSovereignExpirationPolicyTests;
import org.apereo.cas.util.InternalTicketValidatorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    OAuth20IdTokenResponseTypeAuthorizationRequestValidatorTests.class,
    OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidatorTests.class,
    OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidatorTests.class,
    OAuth20TokenResponseTypeAuthorizationRequestValidatorTests.class,
    OAuth20PasswordGrantTypeTokenRequestValidatorTests.class,
    OAuth20ClientCredentialsGrantTypeTokenRequestValidatorTests.class,
    OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests.class,
    OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidatorTests.class,
    OAuth20RevocationRequestValidatorTests.class,
    OAuth20RefreshTokenSovereignExpirationPolicyTests.class,
    OAuth20UserProfileEndpointControllerTests.class,
    OAuth20RevocationEndpointControllerTests.class,
    OAuth20UtilsTests.class,
    OAuth20DefaultRefreshTokenFactoryTests.class,
    OAuth20DefaultOAuthCodeFactoryTests.class,
    OAuth20JwtBuilderTests.class,
    OAuth20ResourceOwnerCredentialsResponseBuilderTests.class,
    OAuth20CallbackAuthorizeEndpointControllerTests.class,
    OAuth20JwtAccessTokenCipherExecutorTests.class,
    OAuth20DefaultDeviceTokenFactoryTests.class,
    OAuth20DefaultAccessTokenFactoryTests.class,
    OAuth20AccessTokenAtHashGeneratorTests.class,
    OAuth20IntrospectionEndpointControllerTests.class,
    OAuth20TokenManagementEndpointTests.class,
    OAuth20JwtAccessTokenEncoderTests.class,
    OAuth20WebApplicationServiceTests.class,
    OAuth20UsernamePasswordAuthenticatorTests.class,
    OAuth20AccessTokenAuthenticatorTests.class,
    OAuth20ClientIdClientSecretAuthenticatorTests.class,
    OAuth20ProofKeyCodeExchangeAuthenticatorTests.class,
    OAuth20RefreshTokenAuthenticatorTests.class,
    OAuth20DefaultUserProfileViewRendererFlatTests.class,
    OAuth20DefaultUserProfileViewRendererNestedTests.class,
    OAuth20AccessTokenGrantRequestAuditResourceResolverTests.class,
    OAuth20UserProfileDataAuditResourceResolverTests.class,
    OAuth20DefaultAccessTokenResponseGeneratorTests.class,
    OAuth20CasCallbackUrlResolverTests.class,
    OAuth20RefreshTokenTests.class,
    OAuth20DeviceTokenExpirationPolicyBuilderTests.class,
    OAuth20DeviceUserCodeApprovalEndpointControllerTests.class,
    AccessTokenPasswordGrantRequestExtractorTests.class,
    AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractorTests.class,
    OAuth20DefaultCasClientRedirectActionBuilderTests.class,
    OAuth20DeviceCodeResponseTypeRequestValidatorTests.class,
    OAuth20HandlerInterceptorAdapterTests.class,
    AccessTokenAuthorizationCodeGrantRequestExtractorTests.class,
    OAuth20DeviceTokenUtilsTests.class,
    AccessTokenRefreshTokenGrantRequestExtractorTests.class,
    TokenSigningAndEncryptionServiceTests.class,
    OAuth20DefaultDeviceUserCodeTests.class,
    OAuth20DefaultCasAuthenticationBuilderTests.class,
    OAuth20TokenSigningAndEncryptionServiceTests.class,
    UnapprovedOAuth20DeviceUserCodeExceptionTests.class,
    InvalidOAuth20DeviceTokenExceptionTests.class,
    DefaultOAuth20UserProfileDataCreatorTests.class,
    OAuth20ClientIdAwareProfileManagerTests.class,
    InternalTicketValidatorTests.class,
    OAuth20TokenGeneratedResultTests.class,
    AccessTokenGrantRequestExtractorTests.class,
    DefaultOAuth20ProfileScopeToAttributesFilterTests.class,
    OAuth20AuthenticationServiceSelectionStrategyTests.class,
    OAuth20ServicesManagerRegisteredServiceLocatorTests.class,
    OAuth20AccessTokenResponseAuditResourceResolverTests.class,
    OAuth20CodeResponseAuditResourceResolverTests.class,
    OAuth20AuthorizationCodeAuthorizationResponseBuilderTests.class,
    OAuth20RegisteredServiceCipherExecutorTests.class,
    OAuth20TokenAuthorizationResponseBuilderTests.class,
    OAuth20InvalidAuthorizationResponseBuilderTests.class,
    OAuth20RegisteredServiceJwtAccessTokenCipherExecutorTests.class,
    CSRFCookieTests.class,
    OAuth20AccessTokenSecurityLogicTests.class
})
@Suite
public class OAuth20TestsSuite {
}
