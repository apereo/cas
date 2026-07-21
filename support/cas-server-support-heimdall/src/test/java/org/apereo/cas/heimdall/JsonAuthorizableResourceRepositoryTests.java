package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResources;
import org.apereo.cas.heimdall.authorizer.resource.policy.RequiredAttributesAuthorizationPolicy;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonAuthorizableResourceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Authorization")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = BaseHeimdallTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/heimdalloidc.jwks",
        "cas.heimdall.json.location=file:${java.io.tmpdir}/policies"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class JsonAuthorizableResourceRepositoryTests {
    @Autowired
    @Qualifier(AuthorizableResourceRepository.BEAN_NAME)
    private AuthorizableResourceRepository authorizableResourceRepository;

    @BeforeAll
    static void ensurePoliciesDirectoryExists() throws Exception {
        val directory = Path.of(System.getProperty("java.io.tmpdir"), "policies");
        Files.createDirectories(directory);
    }

    @Test
    void verifyStoreCreatesNewNamespace() {
        val resource = new AuthorizableResource();
        resource.setId(1);
        resource.setPattern(Pattern.compile("/api/repo/create"));
        resource.setMethod("GET");

        val resources = new AuthorizableResources();
        resources.setNamespace("REPO_TESTS_CREATE");
        resources.getResources().add(resource);

        val stored = authorizableResourceRepository.store(resources);
        assertEquals("REPO_TESTS_CREATE", stored.getNamespace());
        assertEquals(1, stored.getResources().size());

        val found = authorizableResourceRepository.find("REPO_TESTS_CREATE");
        assertEquals(1, found.size());
        assertEquals(1L, found.getFirst().getId());
    }

    @Test
    void verifyStorePersistsToDisk() throws Exception {
        val resource = new AuthorizableResource();
        resource.setId(2);
        resource.setPattern(Pattern.compile("/api/repo/persist"));
        resource.setMethod("POST");

        val resources = new AuthorizableResources();
        resources.setNamespace("REPO_TESTS_PERSIST");
        resources.getResources().add(resource);
        authorizableResourceRepository.store(resources);

        val jsonFile = Path.of(System.getProperty("java.io.tmpdir"), "policies", "REPO_TESTS_PERSIST.json");
        assertTrue(Files.exists(jsonFile));
        val contents = Files.readString(jsonFile);
        assertTrue(contents.contains("REPO_TESTS_PERSIST"));
        assertTrue(contents.contains("/api/repo/persist"));
    }

    @Test
    void verifyStoreOverwritesExistingNamespace() {
        val namespace = "REPO_TESTS_OVERWRITE";

        val original = new AuthorizableResource();
        original.setId(3);
        original.setPattern(Pattern.compile("/api/repo/overwrite/original"));
        original.setMethod("GET");
        val originalResources = new AuthorizableResources();
        originalResources.setNamespace(namespace);
        originalResources.getResources().add(original);
        authorizableResourceRepository.store(originalResources);

        val replacement = new AuthorizableResource();
        replacement.setId(4);
        replacement.setPattern(Pattern.compile("/api/repo/overwrite/replacement"));
        replacement.setMethod("PUT");
        val replacementResources = new AuthorizableResources();
        replacementResources.setNamespace(namespace);
        replacementResources.getResources().add(replacement);
        authorizableResourceRepository.store(replacementResources);

        val found = authorizableResourceRepository.find(namespace);
        assertEquals(1, found.size());
        assertEquals(4L, found.getFirst().getId());
    }

    @Test
    void verifyFindByAuthorizationRequestMatchesPatternAndMethod() {
        val resource = new AuthorizableResource();
        resource.setId(5);
        resource.setPattern(Pattern.compile("/api/repo/find/.+"));
        resource.setMethod("GET");

        val resources = new AuthorizableResources();
        resources.setNamespace("REPO_TESTS_FIND");
        resources.getResources().add(resource);
        authorizableResourceRepository.store(resources);

        val request = AuthorizationRequest.builder()
            .namespace("REPO_TESTS_FIND")
            .uri("/api/repo/find/123")
            .method("GET")
            .build();
        val found = authorizableResourceRepository.find(request);
        assertTrue(found.isPresent());
        assertEquals(5L, found.get().getId());
    }

    @Test
    void verifyFindByAuthorizationRequestSupportsWildcardMethod() {
        val resource = new AuthorizableResource();
        resource.setId(6);
        resource.setPattern(Pattern.compile("/api/repo/wildcard"));
        resource.setMethod("*");

        val resources = new AuthorizableResources();
        resources.setNamespace("REPO_TESTS_WILDCARD");
        resources.getResources().add(resource);
        authorizableResourceRepository.store(resources);

        val request = AuthorizationRequest.builder()
            .namespace("REPO_TESTS_WILDCARD")
            .uri("/api/repo/wildcard")
            .method("DELETE")
            .build();
        assertTrue(authorizableResourceRepository.find(request).isPresent());
    }

    @Test
    void verifyFindByAuthorizationRequestReturnsEmptyWhenNoMatch() {
        val resource = new AuthorizableResource();
        resource.setId(7);
        resource.setPattern(Pattern.compile("/api/repo/nomatch"));
        resource.setMethod("GET");

        val resources = new AuthorizableResources();
        resources.setNamespace("REPO_TESTS_NOMATCH");
        resources.getResources().add(resource);
        authorizableResourceRepository.store(resources);

        val request = AuthorizationRequest.builder()
            .namespace("REPO_TESTS_NOMATCH")
            .uri("/api/repo/other")
            .method("GET")
            .build();
        assertTrue(authorizableResourceRepository.find(request).isEmpty());
    }

    @Test
    void verifyFindByNamespaceAndId() {
        val resource = new AuthorizableResource();
        resource.setId(8);
        resource.setPattern(Pattern.compile("/api/repo/byid"));
        resource.setMethod("GET");

        val resources = new AuthorizableResources();
        resources.setNamespace("REPO_TESTS_BYID");
        resources.getResources().add(resource);
        authorizableResourceRepository.store(resources);

        val found = authorizableResourceRepository.find("REPO_TESTS_BYID", 8);
        assertTrue(found.isPresent());
        assertEquals("/api/repo/byid", found.get().getPattern().pattern());

        assertTrue(authorizableResourceRepository.find("REPO_TESTS_BYID", 999).isEmpty());
    }

    @Test
    void verifyStoreWithPoliciesRetainsPoliciesInMemory() {
        val resource = new AuthorizableResource();
        resource.setId(9);
        resource.setPattern(Pattern.compile("/api/repo/policies"));
        resource.setMethod("GET");
        resource.getPolicies().add(new RequiredAttributesAuthorizationPolicy(Map.of("iss", Set.of("https://issuer.example.org"))));

        val resources = new AuthorizableResources();
        resources.setNamespace("REPO_TESTS_POLICIES");
        resources.getResources().add(resource);
        authorizableResourceRepository.store(resources);

        val found = authorizableResourceRepository.find("REPO_TESTS_POLICIES");
        assertEquals(1, found.getFirst().getPolicies().size());
    }

    @Test
    void verifyFindAllContainsStoredNamespaces() {
        val resource = new AuthorizableResource();
        resource.setId(10);
        resource.setPattern(Pattern.compile("/api/repo/findall"));
        resource.setMethod("GET");

        val resources = new AuthorizableResources();
        resources.setNamespace("REPO_TESTS_FINDALL");
        resources.getResources().add(resource);
        authorizableResourceRepository.store(resources);

        val all = authorizableResourceRepository.findAll();
        assertTrue(all.containsKey("REPO_TESTS_FINDALL"));
        assertEquals(1, all.get("REPO_TESTS_FINDALL").size());
    }
}
