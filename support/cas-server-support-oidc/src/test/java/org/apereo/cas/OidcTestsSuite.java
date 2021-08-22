package org.apereo.cas;

import org.apereo.cas.oidc.authn.OidcAccessTokenAuthenticatorTests;
import org.apereo.cas.oidc.authn.OidcClientConfigurationAccessTokenAuthenticatorTests;
import org.apereo.cas.oidc.authn.OidcClientSecretJwtAuthenticatorTests;
import org.apereo.cas.oidc.authn.OidcPrivateKeyJwtAuthenticatorTests;
import org.apereo.cas.oidc.claims.OidcAddressScopeAttributeReleasePolicyTests;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicyTests;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicyTests;
import org.apereo.cas.oidc.claims.OidcOpenIdScopeAttributeReleasePolicyTests;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicyTests;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicyTests;
import org.apereo.cas.oidc.claims.mapping.OidcDefaultAttributeToScopeClaimMapperTests;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettingsFactoryTests;
import org.apereo.cas.oidc.discovery.webfinger.OidcEchoingWebFingerUserInfoRepositoryTests;
import org.apereo.cas.oidc.discovery.webfinger.OidcGroovyWebFingerUserInfoRepositoryTests;
import org.apereo.cas.oidc.discovery.webfinger.OidcRestfulWebFingerUserInfoRepositoryTests;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryServiceTests;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerUserInfoRepositoryTests;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponseTests;
import org.apereo.cas.oidc.issuer.OidcDefaultIssuerServiceTests;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoaderTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreJacksonDeserializerTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtilsTests;
import org.apereo.cas.oidc.jwks.OidcRegisteredServiceJsonWebKeystoreCacheLoaderTests;
import org.apereo.cas.oidc.jwks.OidcServiceJsonWebKeystoreCacheExpirationPolicyTests;
import org.apereo.cas.oidc.jwks.generator.OidcDefaultJsonWebKeystoreGeneratorServiceTests;
import org.apereo.cas.oidc.jwks.generator.OidcRestfulJsonWebKeystoreGeneratorServiceTests;
import org.apereo.cas.oidc.profile.OidcProfileScopeToAttributesFilterTests;
import org.apereo.cas.oidc.profile.OidcUserProfileDataCreatorTests;
import org.apereo.cas.oidc.profile.OidcUserProfileSigningAndEncryptionServiceTests;
import org.apereo.cas.oidc.profile.OidcUserProfileViewRendererDefaultTests;
import org.apereo.cas.oidc.profile.OidcUserProfileViewRendererFlatTests;
import org.apereo.cas.oidc.scopes.DefaultOidcAttributeReleasePolicyFactoryTests;
import org.apereo.cas.oidc.services.OidcServiceRegistryListenerTests;
import org.apereo.cas.oidc.services.OidcServicesManagerRegisteredServiceLocatorTests;
import org.apereo.cas.oidc.slo.OidcSingleLogoutMessageCreatorTests;
import org.apereo.cas.oidc.slo.OidcSingleLogoutServiceLogoutUrlBuilderTests;
import org.apereo.cas.oidc.slo.OidcSingleLogoutServiceMessageHandlerTests;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorServiceTests;
import org.apereo.cas.oidc.token.OidcIdTokenSigningAndEncryptionServiceTests;
import org.apereo.cas.oidc.token.OidcJwtAccessTokenCipherExecutorTests;
import org.apereo.cas.oidc.token.OidcJwtAccessTokenEncoderTests;
import org.apereo.cas.oidc.token.OidcRegisteredServiceJwtAccessTokenCipherExecutorNoCacheTests;
import org.apereo.cas.oidc.token.OidcRegisteredServiceJwtAccessTokenCipherExecutorTests;
import org.apereo.cas.oidc.util.OidcRequestSupportTests;
import org.apereo.cas.oidc.web.OidcAccessTokenResponseGeneratorTests;
import org.apereo.cas.oidc.web.OidcAuthenticationAuthorizeSecurityLogicTests;
import org.apereo.cas.oidc.web.OidcAuthorizationModelAndViewBuilderTests;
import org.apereo.cas.oidc.web.OidcCallbackAuthorizeViewResolverTests;
import org.apereo.cas.oidc.web.OidcCasCallbackUrlResolverTests;
import org.apereo.cas.oidc.web.OidcCasClientRedirectActionBuilderTests;
import org.apereo.cas.oidc.web.OidcConsentApprovalViewResolverTests;
import org.apereo.cas.oidc.web.OidcHandlerInterceptorAdapterTests;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAndTokenAuthorizationResponseBuilderTests;
import org.apereo.cas.oidc.web.OidcImplicitIdTokenAuthorizationResponseBuilderTests;
import org.apereo.cas.oidc.web.OidcLocaleChangeInterceptorTests;
import org.apereo.cas.oidc.web.controllers.OidcIntrospectionEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.OidcWellKnownEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.authorize.OidcAuthorizeEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientConfigurationEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcDynamicClientRegistrationEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.jwks.OidcJwksEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.logout.OidcLogoutEndpointControllerMatcherTests;
import org.apereo.cas.oidc.web.controllers.logout.OidcLogoutEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.profile.OidcUserProfileEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.token.OidcAccessTokenEndpointControllerTests;
import org.apereo.cas.oidc.web.controllers.token.OidcRevocationEndpointControllerTests;
import org.apereo.cas.oidc.web.flow.OidcAuthenticationContextWebflowEventResolverTests;
import org.apereo.cas.oidc.web.flow.OidcCasWebflowLoginContextProviderTests;
import org.apereo.cas.oidc.web.flow.OidcMultifactorAuthenticationTriggerTests;
import org.apereo.cas.oidc.web.flow.OidcRegisteredServiceUIActionTests;
import org.apereo.cas.oidc.web.flow.OidcWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link OidcTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    OidcWellKnownEndpointControllerTests.class,
    OidcIdTokenGeneratorServiceTests.class,
    OidcIdTokenSigningAndEncryptionServiceTests.class,
    OidcDefaultJsonWebKeystoreGeneratorServiceTests.class,
    OidcDefaultJsonWebKeystoreCacheLoaderTests.class,
    OidcAuthenticationContextWebflowEventResolverTests.class,
    OidcProfileScopeToAttributesFilterTests.class,
    OidcServerDiscoverySettingsFactoryTests.class,
    OidcRegisteredServiceUIActionTests.class,
    OidcRegisteredServiceJsonWebKeystoreCacheLoaderTests.class,
    OidcRequestSupportTests.class,
    OidcPrivateKeyJwtAuthenticatorTests.class,
    OidcClientSecretJwtAuthenticatorTests.class,
    OidcEchoingWebFingerUserInfoRepositoryTests.class,
    OidcGroovyWebFingerUserInfoRepositoryTests.class,
    OidcServiceRegistryListenerTests.class,
    OidcUserProfileDataCreatorTests.class,
    OidcHandlerInterceptorAdapterTests.class,
    OidcClientConfigurationEndpointControllerTests.class,
    OidcJwtAccessTokenEncoderTests.class,
    OidcUserProfileSigningAndEncryptionServiceTests.class,
    OidcJsonWebKeyStoreUtilsTests.class,
    OidcDynamicClientRegistrationEndpointControllerTests.class,
    OidcRegisteredServiceJwtAccessTokenCipherExecutorNoCacheTests.class,
    OidcUserProfileViewRendererDefaultTests.class,
    OidcUserProfileViewRendererFlatTests.class,
    OidcAccessTokenResponseGeneratorTests.class,
    OidcIntrospectionEndpointControllerTests.class,
    OidcLogoutEndpointControllerTests.class,
    OidcLogoutEndpointControllerMatcherTests.class,
    OidcRestfulWebFingerUserInfoRepositoryTests.class,
    OidcAddressScopeAttributeReleasePolicyTests.class,
    OidcCustomScopeAttributeReleasePolicyTests.class,
    OidcEmailScopeAttributeReleasePolicyTests.class,
    OidcPhoneScopeAttributeReleasePolicyTests.class,
    OidcProfileScopeAttributeReleasePolicyTests.class,
    OidcDefaultAttributeToScopeClaimMapperTests.class,
    OidcAccessTokenAuthenticatorTests.class,
    OidcWebflowConfigurerTests.class,
    OidcJwtAccessTokenCipherExecutorTests.class,
    OidcAccessTokenEndpointControllerTests.class,
    OidcRevocationEndpointControllerTests.class,
    OidcServicesManagerRegisteredServiceLocatorTests.class,
    OidcOpenIdScopeAttributeReleasePolicyTests.class,
    OidcSingleLogoutServiceLogoutUrlBuilderTests.class,
    OidcImplicitIdTokenAuthorizationResponseBuilderTests.class,
    OidcJwksEndpointControllerTests.class,
    OidcServiceJsonWebKeystoreCacheExpirationPolicyTests.class,
    OidcAuthorizeEndpointControllerTests.class,
    OidcUserProfileEndpointControllerTests.class,
    OidcJsonWebKeyStoreJacksonDeserializerTests.class,
    OidcMultifactorAuthenticationTriggerTests.class,
    OidcWebFingerDiscoveryServiceTests.class,
    OidcCasClientRedirectActionBuilderTests.class,
    OidcConsentApprovalViewResolverTests.class,
    OidcWebFingerUserInfoRepositoryTests.class,
    OidcClientRegistrationResponseTests.class,
    OidcCallbackAuthorizeViewResolverTests.class,
    OidcImplicitIdTokenAndTokenAuthorizationResponseBuilderTests.class,
    OidcRestfulJsonWebKeystoreGeneratorServiceTests.class,
    OidcRegisteredServiceJwtAccessTokenCipherExecutorTests.class,
    OidcClientConfigurationAccessTokenAuthenticatorTests.class,
    OidcDefaultIssuerServiceTests.class,
    OidcAuthorizationModelAndViewBuilderTests.class,
    OidcLocaleChangeInterceptorTests.class,
    OidcCasWebflowLoginContextProviderTests.class,
    OidcCasCallbackUrlResolverTests.class,
    OidcAuthenticationAuthorizeSecurityLogicTests.class,
    DefaultOidcAttributeReleasePolicyFactoryTests.class,
    OidcSingleLogoutMessageCreatorTests.class,
    OidcSingleLogoutServiceMessageHandlerTests.class
})
@Suite
public class OidcTestsSuite {
}
