package org.apereo.cas;

import org.apereo.cas.oidc.authn.OidcClientSecretJwtAuthenticatorTests;
import org.apereo.cas.oidc.authn.OidcPrivateKeyJwtAuthenticatorTests;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettingsFactoryTests;
import org.apereo.cas.oidc.discovery.webfinger.OidcEchoingWebFingerUserInfoRepositoryTests;
import org.apereo.cas.oidc.discovery.webfinger.OidcGroovyWebFingerUserInfoRepositoryTests;
import org.apereo.cas.oidc.discovery.webfinger.OidcRestfulWebFingerUserInfoRepositoryTests;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoaderTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorServiceTests;
import org.apereo.cas.oidc.jwks.OidcServiceJsonWebKeystoreCacheLoaderTests;
import org.apereo.cas.oidc.profile.OidcProfileScopeToAttributesFilterTests;
import org.apereo.cas.oidc.services.OidcServiceRegistryListenerTests;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorServiceTests;
import org.apereo.cas.oidc.token.OidcIdTokenSigningAndEncryptionServiceTests;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupportTests;
import org.apereo.cas.oidc.web.controllers.OidcWellKnownEndpointControllerTests;
import org.apereo.cas.oidc.web.flow.OidcAuthenticationContextWebflowEventResolverTests;
import org.apereo.cas.oidc.web.flow.OidcRegisteredServiceUIActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    OidcJsonWebKeystoreGeneratorServiceTests.class,
    OidcDefaultJsonWebKeystoreCacheLoaderTests.class,
    OidcAuthenticationContextWebflowEventResolverTests.class,
    OidcProfileScopeToAttributesFilterTests.class,
    OidcServerDiscoverySettingsFactoryTests.class,
    OidcRegisteredServiceUIActionTests.class,
    OidcServiceJsonWebKeystoreCacheLoaderTests.class,
    OidcAuthorizationRequestSupportTests.class,
    OidcPrivateKeyJwtAuthenticatorTests.class,
    OidcClientSecretJwtAuthenticatorTests.class,
    OidcEchoingWebFingerUserInfoRepositoryTests.class,
    OidcGroovyWebFingerUserInfoRepositoryTests.class,
    OidcServiceRegistryListenerTests.class,
    OidcRestfulWebFingerUserInfoRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class OidcTestsSuite {
}
