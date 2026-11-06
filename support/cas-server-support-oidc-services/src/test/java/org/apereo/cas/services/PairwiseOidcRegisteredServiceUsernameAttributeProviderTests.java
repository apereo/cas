package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;
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

    /**
     * A generator without a salt of its own calculates identifiers with the pairwise subject salt of CAS, which is not
     * written into it: clients of the same sector share identifiers, and clients of other sectors do not.
     */
    @Test
    void verifyPairwiseSubjectSaltOfCasIsUsedAndNotStored() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.getEnvironment().getPropertySources().addFirst(new MapPropertySource("salt",
            Map.<String, Object>of(PairwiseOidcRegisteredServiceUsernameAttributeProvider.PAIRWISE_SUBJECT_SALT_PROPERTY, "global-salt")));
        applicationContext.refresh();
        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser");
        val uids = new ArrayList<String>();
        for (val redirectUri : List.of("https://app.example.org/cb", "https://app.example.org/other", "https://other.example.org/cb")) {
            val registeredService = new OidcRegisteredService();
            registeredService.setClientId(UUID.randomUUID().toString());
            registeredService.setSubjectType(OidcSubjectTypes.PAIRWISE.getType());
            registeredService.setServiceId(OidcRedirectUriUtils.toServiceId(List.of(redirectUri)));
            uids.add(provider.resolveUsername(RegisteredServiceUsernameProviderContext.builder()
                .registeredService(registeredService)
                .service(RegisteredServiceTestUtils.getService())
                .principal(principal)
                .applicationContext(applicationContext)
                .build()));
        }
        assertEquals(new ShibbolethCompatiblePersistentIdGenerator("global-salt").generate(principal, "app.example.org"), uids.getFirst());
        assertEquals(uids.getFirst(), uids.get(1));
        assertNotEquals(uids.getFirst(), uids.get(2));
        assertTrue(StringUtils.isBlank(((OidcPairwisePersistentIdGenerator) provider.getPersistentIdGenerator()).getSalt()));
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

    /**
     * A generator of a type of its own without a salt keeps its type when it calculates identifiers with the pairwise
     * subject salt of CAS. Only web redirect URIs on hosts other than loopback ones give a sector host to a client
     * registered by CAS: one with a private-use scheme or loopback redirect URIs is its own sector, whatever its redirect
     * URIs name. A service id defined by hand is read as it always was, a regular expression included, and one that names
     * no host is its own sector too.
     */
    @Test
    void verifyCustomGeneratorAndNativeSectors() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.getEnvironment().getPropertySources().addFirst(new MapPropertySource("salt",
            Map.<String, Object>of(PairwiseOidcRegisteredServiceUsernameAttributeProvider.PAIRWISE_SUBJECT_SALT_PROPERTY, "global-salt")));
        applicationContext.refresh();
        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
        provider.setPersistentIdGenerator(new CustomPersistentIdGenerator());
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser");
        val registeredService = new OidcRegisteredService();
        registeredService.setClientId("native-client");
        registeredService.setSubjectType(OidcSubjectTypes.PAIRWISE.getType());
        registeredService.setServiceId(OidcRedirectUriUtils.toServiceId(List.of("https://app.example.org/cb")));
        val context = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(registeredService)
            .service(RegisteredServiceTestUtils.getService())
            .principal(principal)
            .applicationContext(applicationContext)
            .build();
        val salted = new ShibbolethCompatiblePersistentIdGenerator("global-salt");
        assertEquals("custom:" + salted.generate(principal, "app.example.org"), provider.resolveUsername(context));

        registeredService.setServiceId(OidcRedirectUriUtils.toServiceId(List.of("com.example.app://app.example.org/cb")));
        assertEquals("custom:" + salted.generate(principal, "native-client"), provider.resolveUsername(context));
        registeredService.setServiceId(OidcRedirectUriUtils.toServiceId(List.of("http://127.0.0.1:3000/cb", "http://localhost/cb")));
        assertEquals("custom:" + salted.generate(principal, "native-client"), provider.resolveUsername(context));
        registeredService.setServiceId("^https://app.example.org/cb");
        assertEquals("custom:" + salted.generate(principal, "app.example.org"), provider.resolveUsername(context));
        registeredService.setServiceId("not-a-uri");
        assertEquals("custom:" + salted.generate(principal, "native-client"), provider.resolveUsername(context));
        registeredService.setServiceId("^https?://app.example.org/cb");
        assertEquals("custom:" + salted.generate(principal, "native-client"), provider.resolveUsername(context));
        registeredService.setServiceId("^https://.*");
        assertEquals("custom:" + salted.generate(principal, ".*"), provider.resolveUsername(context));
        registeredService.setServiceId("com.example.app://callback");
        assertEquals("custom:" + salted.generate(principal, "callback"), provider.resolveUsername(context));
        registeredService.setServiceId(OidcRedirectUriUtils.toServiceId(List.of("https://app.example.org/cb")));
        registeredService.setSectorIdentifierUri("urn:example:sector");
        assertEquals("custom:" + salted.generate(principal, "native-client"), provider.resolveUsername(context));
        registeredService.setSectorIdentifierUri(null);

        provider.setPersistentIdGenerator(new UncopyablePersistentIdGenerator());
        val fallback = new OidcPairwisePersistentIdGenerator();
        fallback.setSalt("global-salt");
        assertEquals(fallback.generate(principal, "app.example.org"), provider.resolveUsername(context));
        assertEquals(fallback.generate(principal, "app.example.org"), provider.resolveUsername(context));
        assertTrue(StringUtils.isBlank(((ShibbolethCompatiblePersistentIdGenerator) provider.getPersistentIdGenerator()).getSalt()));
        assertEquals(List.of(StringUtils.EMPTY), OidcRedirectUriUtils.toSectors(List.of("http://127.0.0.1:3000/cb", "http://localhost:8080/cb",
            "https://[::1]/cb", "http://app.localhost/cb")));
        assertEquals(List.of(StringUtils.EMPTY), OidcRedirectUriUtils.toSectors(List.of("com.example.app://app.example.org/cb", "myapp:///cb")));
        assertEquals(2, OidcRedirectUriUtils.toSectors(List.of("https://app.example.org/cb", "com.example.app:/cb")).size());
        assertThrows(IllegalArgumentException.class, () -> OidcRedirectUriUtils.toSectors(List.of("https:/app.example.org/cb")));
        assertThrows(IllegalArgumentException.class, () -> OidcRedirectUriUtils.toSectors(List.of("https:///cb")));
    }

    static class UncopyablePersistentIdGenerator extends ShibbolethCompatiblePersistentIdGenerator {
        @Serial
        private static final long serialVersionUID = -2716314826417404190L;

        @Getter
        private final Object state = new Object();

        @Override
        public String generate(final Principal principal, final String service) {
            return "uncopyable:" + super.generate(principal, service);
        }
    }

    static class CustomPersistentIdGenerator extends ShibbolethCompatiblePersistentIdGenerator {
        @Serial
        private static final long serialVersionUID = 6020431578712215364L;

        @Override
        public String generate(final Principal principal, final String service) {
            return "custom:" + super.generate(principal, service);
        }
    }
}
