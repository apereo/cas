package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.mcp.BaseCasMcpTests;
import org.apereo.cas.mcp.McpFederationTestConfiguration;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasMcpFederationToolsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = BaseCasMcpTests.SharedTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(CasTestExtension.class)
class CasMcpFederationToolsTests extends BaseCasMcpTests {
    @Autowired
    @Qualifier("casSamlIdPMetadataTools")
    private CasSamlIdPMetadataTools casSamlIdPMetadataTools;

    @Autowired
    @Qualifier("casOidcTools")
    private CasOidcTools casOidcTools;

    @Autowired
    @Qualifier("casServiceManagementTools")
    private CasServiceManagementTools casServiceManagementTools;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    private SamlRegisteredService saveSamlService(final String entityId, final String metadataLocation) {
        val service = new SamlRegisteredService();
        service.setId(ThreadLocalRandom.current().nextLong(1, Integer.MAX_VALUE));
        service.setName("SAML-" + service.getId());
        service.setServiceId("^" + Pattern.quote(entityId) + "$");
        service.setMetadataLocation(metadataLocation);
        servicesManager.save(service);
        return service;
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyResolveSamlMetadata() {
        val entityId = "https://sp-" + UUID.randomUUID() + ".example.org";
        val service = saveSamlService(entityId, "https://md.example.org/" + UUID.randomUUID());
        val result = casSamlIdPMetadataTools.resolveSamlMetadata(entityId);
        assertTrue(result.resolved(), result::toString);
        assertEquals(service.getId(), Objects.requireNonNull(result.registeredService()).id());
        assertEquals(Boolean.FALSE, result.expired());
        val sp = Objects.requireNonNull(result.serviceProvider());
        assertTrue(sp.wantAssertionsSigned());
        val acs = sp.assertionConsumerServices().getFirst();
        assertEquals(entityId + "/acs", acs.location());
        val key = sp.keys().getFirst();
        assertEquals("SIGNING", key.use());
        val certificate = Objects.requireNonNull(key.certificates()).getFirst();
        assertTrue(certificate.subject().contains("CN=sp.example.org"), certificate::toString);
        assertFalse(certificate.expired());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyBrokenAndExpiredSamlMetadata() {
        val brokenEntityId = "https://broken-" + UUID.randomUUID() + ".example.org";
        saveSamlService(brokenEntityId, "https://md.example.org/broken/" + UUID.randomUUID());
        val broken = casSamlIdPMetadataTools.resolveSamlMetadata(brokenEntityId);
        assertFalse(broken.resolved());
        assertTrue(Objects.requireNonNull(broken.error()).contains("Unable to fetch metadata"), broken::toString);

        val expiredEntityId = "https://expired-" + UUID.randomUUID() + ".example.org";
        saveSamlService(expiredEntityId, "https://md.example.org/expired/" + UUID.randomUUID());
        val expired = casSamlIdPMetadataTools.resolveSamlMetadata(expiredEntityId);
        assertFalse(expired.resolved());
        assertEquals(Boolean.TRUE, expired.expired());

        val unknown = casSamlIdPMetadataTools.resolveSamlMetadata("https://unknown-" + UUID.randomUUID() + ".example.net");
        assertFalse(unknown.resolved());
        assertTrue(Objects.requireNonNull(unknown.reason()).startsWith("No SAML2 service provider registration"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindOidcClient() {
        val clientId = "client-" + UUID.randomUUID();
        val service = new OidcRegisteredService();
        service.setId(ThreadLocalRandom.current().nextLong(1, Integer.MAX_VALUE));
        service.setName("OIDC-" + service.getId());
        service.setServiceId("^https://oidc-" + UUID.randomUUID() + ".example.org/.*");
        service.setClientId(clientId);
        service.setClientSecret("very-secret-client-secret");
        service.setSupportedGrantTypes(new HashSet<>(Set.of("authorization_code")));
        service.setScopes(new HashSet<>(Set.of("openid", "profile")));
        service.setAudience(new HashSet<>(Set.of("https://audience.example.org")));
        service.setAllowedResources(new HashSet<>(Set.of("https://api\\.example\\.org/(orders|reports)")));
        servicesManager.save(service);

        val result = casOidcTools.findOidcClient(clientId);
        assertEquals(service.getId(), result.id());
        assertEquals(List.of("https://audience.example.org"), result.audience());
        assertEquals(List.of("https://api\\.example\\.org/(orders|reports)"), result.allowedResources());
        assertEquals(Boolean.TRUE, result.clientSecretConfigured());
        assertEquals(List.of("openid", "profile"), result.scopes());
        assertEquals(McpFederationTestConfiguration.OIDC_ISSUER, result.issuer());
        assertFalse(result.toString().contains("very-secret-client-secret"));
        val definition = casServiceManagementTools.findService(service.getId());
        assertNotNull(definition);
        assertFalse(definition.contains("very-secret-client-secret"), definition);
        assertTrue(definition.contains("\"clientSecrets\":\"<redacted>\""), definition);
        assertTrue(definition.contains(clientId), definition);
        assertNotNull(casOidcTools.findOidcClient("unknown-" + UUID.randomUUID()).reason());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyCheckOidcClientResource() {
        val clientId = "client-" + UUID.randomUUID();
        val service = new OidcRegisteredService();
        service.setId(ThreadLocalRandom.current().nextLong(1, Integer.MAX_VALUE));
        service.setName("OIDC-" + service.getId());
        service.setServiceId("^https://oidc-" + UUID.randomUUID() + ".example.org/.*");
        service.setClientId(clientId);
        service.setAllowedResources(new HashSet<>(Set.of("https://api\\.example\\.org/(orders|reports)")));
        servicesManager.save(service);

        val allowed = casOidcTools.checkOidcClientResource(clientId, "https://api.example.org/orders");
        assertTrue(allowed.allowed());
        assertEquals("https://api\\.example\\.org/(orders|reports)", allowed.matchedPattern());
        assertEquals(List.of(clientId, "https://api.example.org/orders"), allowed.audience());
        assertEquals("client", allowed.matchedBy());
        assertEquals(List.of("https://cas\\.example\\.org/cas/mcp"), allowed.casAllowedResources());

        val casWide = casOidcTools.checkOidcClientResource(clientId, "https://cas.example.org/cas/mcp");
        assertTrue(casWide.allowed());
        assertEquals("cas.authn.oauth.core.allowed-resources", casWide.matchedBy());
        assertEquals("https://cas\\.example\\.org/cas/mcp", casWide.matchedPattern());

        val partial = casOidcTools.checkOidcClientResource(clientId, "https://api.example.org/orders/../admin");
        assertFalse(partial.allowed());
        assertNotNull(partial.reason());
        assertFalse(casOidcTools.checkOidcClientResource(clientId, "https://api.example.org/orders#fragment").allowed());
        assertFalse(casOidcTools.checkOidcClientResource(clientId, "/orders").allowed());
        assertNotNull(casOidcTools.checkOidcClientResource("unknown-" + UUID.randomUUID(), "https://api.example.org/orders").reason());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyListJwksKeys() {
        val result = casOidcTools.listJwksKeys();
        assertEquals(McpFederationTestConfiguration.OIDC_ISSUER, result.issuer());
        val keys = result.keys();
        assertEquals(4, keys.size());
        assertTrue(keys.stream().anyMatch(key -> "CURRENT".equals(key.state())));
        assertEquals(List.of("keyId", "keyType", "algorithm", "use", "state"),
            Arrays.stream(CasOidcTools.JwksKey.class.getRecordComponents()).map(RecordComponent::getName).toList());
    }

    @Test
    @WithMockUser(roles = "USER")
    void verifyToolsDenyUsersWithoutRole() {
        assertThrows(AccessDeniedException.class, () -> casSamlIdPMetadataTools.resolveSamlMetadata("https://sp.example.org"));
        assertThrows(AccessDeniedException.class, () -> casOidcTools.findOidcClient("client"));
        assertThrows(AccessDeniedException.class, () -> casOidcTools.listJwksKeys());
        assertThrows(AccessDeniedException.class, () -> casOidcTools.checkOidcClientResource("client", "https://api.example.org/"));
    }
}
