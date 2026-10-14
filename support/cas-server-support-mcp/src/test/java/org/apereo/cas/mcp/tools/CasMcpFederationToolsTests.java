package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.mcp.BaseCasMcpTests;
import org.apereo.cas.mcp.McpFederationTestConfiguration;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
class CasMcpFederationToolsTests extends BaseCasMcpTests {
    @Autowired
    @Qualifier("casSamlIdPMetadataTools")
    private CasSamlIdPMetadataTools casSamlIdPMetadataTools;

    @Autowired
    @Qualifier("casOidcTools")
    private CasOidcTools casOidcTools;

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
        assertEquals(Boolean.TRUE, result.get("resolved"), result::toString);
        assertEquals(service.getId(), ((Map<?, ?>) result.get("registeredService")).get("id"));
        assertEquals(Boolean.FALSE, result.get("expired"));
        val sp = (Map<?, ?>) result.get("serviceProvider");
        assertEquals(Boolean.TRUE, sp.get("wantAssertionsSigned"));
        val acs = (Map<?, ?>) ((List<?>) sp.get("assertionConsumerServices")).getFirst();
        assertEquals(entityId + "/acs", acs.get("location"));
        val key = (Map<?, ?>) ((List<?>) sp.get("keys")).getFirst();
        assertEquals("SIGNING", key.get("use"));
        val certificate = (Map<?, ?>) ((List<?>) key.get("certificates")).getFirst();
        assertTrue(certificate.get("subject").toString().contains("CN=sp.example.org"), certificate::toString);
        assertEquals(Boolean.FALSE, certificate.get("expired"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyBrokenAndExpiredSamlMetadata() {
        val brokenEntityId = "https://broken-" + UUID.randomUUID() + ".example.org";
        saveSamlService(brokenEntityId, "https://md.example.org/broken/" + UUID.randomUUID());
        val broken = casSamlIdPMetadataTools.resolveSamlMetadata(brokenEntityId);
        assertEquals(Boolean.FALSE, broken.get("resolved"));
        assertTrue(broken.get("error").toString().contains("Unable to fetch metadata"), broken::toString);

        val expiredEntityId = "https://expired-" + UUID.randomUUID() + ".example.org";
        saveSamlService(expiredEntityId, "https://md.example.org/expired/" + UUID.randomUUID());
        val expired = casSamlIdPMetadataTools.resolveSamlMetadata(expiredEntityId);
        assertEquals(Boolean.FALSE, expired.get("resolved"));
        assertEquals(Boolean.TRUE, expired.get("expired"));

        val unknown = casSamlIdPMetadataTools.resolveSamlMetadata("https://unknown-" + UUID.randomUUID() + ".example.net");
        assertEquals(Boolean.FALSE, unknown.get("resolved"));
        assertTrue(unknown.get("reason").toString().startsWith("No SAML2 service provider registration"));
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
        servicesManager.save(service);

        val result = casOidcTools.findOidcClient(clientId);
        assertEquals(service.getId(), result.get("id"));
        assertEquals(Boolean.TRUE, result.get("clientSecretConfigured"));
        assertEquals(new TreeSet<>(Set.of("openid", "profile")), result.get("scopes"));
        assertEquals(McpFederationTestConfiguration.OIDC_ISSUER, result.get("issuer"));
        assertFalse(result.toString().contains("very-secret-client-secret"));
        assertTrue(casOidcTools.findOidcClient("unknown-" + UUID.randomUUID()).containsKey("reason"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyListJwksKeys() {
        val result = casOidcTools.listJwksKeys();
        assertEquals(McpFederationTestConfiguration.OIDC_ISSUER, result.get("issuer"));
        val keys = (List<?>) result.get("keys");
        assertEquals(4, keys.size());
        assertTrue(keys.stream().map(Map.class::cast).anyMatch(key -> "CURRENT".equals(key.get("state"))));
        assertTrue(keys.stream().map(Map.class::cast).allMatch(key -> key.keySet().equals(Set.of("keyId", "keyType", "algorithm", "use", "state"))));
    }

    @Test
    @WithMockUser(roles = "USER")
    void verifyToolsDenyUsersWithoutRole() {
        assertThrows(AccessDeniedException.class, () -> casSamlIdPMetadataTools.resolveSamlMetadata("https://sp.example.org"));
        assertThrows(AccessDeniedException.class, () -> casOidcTools.findOidcClient("client"));
        assertThrows(AccessDeniedException.class, () -> casOidcTools.listJwksKeys());
    }
}
