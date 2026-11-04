package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PairwiseOidcRegisteredServiceUsernameAttributeProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAttributes")
class PairwiseOidcRegisteredServiceUsernameAttributeProviderTests {
    @Test
    void verifyNonCompatibleService() throws Throwable {
        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService("verifyUsernameByPrincipalAttributeWithMapping"))
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser"))
            .applicationContext(applicationContext)
            .build();
        val uid = provider.resolveUsername(usernameContext);
        assertEquals("casuser", uid);
    }

    @Test
    void verifyUndefinedOrPublicSubjectType() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();

        val registeredService = new OidcRegisteredService();
        registeredService.setName("verifyUndefinedOrPublicSubjectType");
        registeredService.setServiceId("testId");
        registeredService.setClientId("clientid");
        registeredService.setClientSecrets(List.of(OAuthRegisteredServiceClientSecret.withoutExpiration("something")));

        registeredService.setSubjectType(StringUtils.EMPTY);

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(registeredService)
            .service(RegisteredServiceTestUtils.getService())
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser"))
            .applicationContext(applicationContext)
            .build();
        var uid = provider.resolveUsername(usernameContext);
        assertEquals("casuser", uid);

        registeredService.setSubjectType(null);
        uid = provider.resolveUsername(usernameContext);
        assertEquals("casuser", uid);

        registeredService.setSubjectType(OidcSubjectTypes.PUBLIC.getType());
        uid = provider.resolveUsername(usernameContext);
        assertEquals("casuser", uid);
    }

    @Test
    void verifySubjectType() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        
        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
        provider.setPersistentIdGenerator(new ShibbolethCompatiblePersistentIdGenerator("cpaOl1pwGZ439!!"));

        val registeredService = new OidcRegisteredService();
        registeredService.setName("verifySubjectType");
        registeredService.setSectorIdentifierUri("https://sso.example.org/oidc");
        registeredService.setClientId("clientid");
        registeredService.setClientSecrets(List.of(OAuthRegisteredServiceClientSecret.withoutExpiration("something")));
        registeredService.setSubjectType(OidcSubjectTypes.PAIRWISE.getType());

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(registeredService)
            .service(RegisteredServiceTestUtils.getService())
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser"))
            .applicationContext(applicationContext)
            .build();

        val uid = provider.resolveUsername(usernameContext);
        assertEquals("9IOlxFj2XgfhkNJieynbw+Pm+4E=", uid);

        registeredService.setSectorIdentifierUri(null);
        registeredService.setServiceId("https://sso.example.org/oidc");
        val uid1 = provider.resolveUsername(usernameContext);
        assertEquals(uid1, uid);
    }

    @Test
    void verifySectorIdentifierOfRegisteredRedirectUris() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
        provider.setPersistentIdGenerator(new ShibbolethCompatiblePersistentIdGenerator("cpaOl1pwGZ439!!"));

        val registeredService = new OidcRegisteredService();
        registeredService.setName("verifySectorIdentifierOfRegisteredRedirectUris");
        registeredService.setClientId("clientid");
        registeredService.setSubjectType(OidcSubjectTypes.PAIRWISE.getType());
        registeredService.setServiceId(OidcRedirectUriUtils.toServiceId(List.of("https://sso.example.org/cb?x=1", "https://sso.example.org/other")));
        assertTrue(registeredService.matches("https://sso.example.org/cb?x=1"));
        assertFalse(registeredService.matches("https://SSO.example.org/cb?x=1"));
        assertFalse(registeredService.matches("https://sso.example.org/cbXx=1"));
        assertEquals(List.of("https://sso.example.org/cb?x=1", "https://sso.example.org/other"),
            OidcRedirectUriUtils.toRedirectUris(registeredService.getServiceId()));

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(registeredService)
            .service(RegisteredServiceTestUtils.getService())
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser"))
            .applicationContext(applicationContext)
            .build();
        assertEquals("9IOlxFj2XgfhkNJieynbw+Pm+4E=", provider.resolveUsername(usernameContext));

        registeredService.setServiceId("^(\\Qhttps://sso.example.org/cb\\E|\\Qhttps://other.example.org/cb\\E)$");
        assertEquals(List.of("https://sso.example.org/cb", "https://other.example.org/cb"),
            OidcRedirectUriUtils.toRedirectUris(registeredService.getServiceId()));
        assertEquals("9IOlxFj2XgfhkNJieynbw+Pm+4E=", provider.resolveUsername(usernameContext));

        registeredService.setServiceId(OidcRedirectUriUtils.toServiceId(List.of("com.example.app:/callback")));
        val nativeUid = provider.resolveUsername(usernameContext);
        assertNotEquals("9IOlxFj2XgfhkNJieynbw+Pm+4E=", nativeUid);
        registeredService.setClientId("otherclientid");
        assertNotEquals(nativeUid, provider.resolveUsername(usernameContext));
    }
}
