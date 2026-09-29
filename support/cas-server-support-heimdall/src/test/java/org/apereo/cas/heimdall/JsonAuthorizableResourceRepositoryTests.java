package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authorizer.DefaultResourceAuthorizer;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.repository.JsonAuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResources;
import org.apereo.cas.heimdall.authorizer.resource.policy.RequiredAttributesAuthorizationPolicy;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.heimdall.engine.DefaultAuthorizationEngine;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.awaitility.Awaitility.*;
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
    private static final Duration WATCHER_TIMEOUT = Duration.ofSeconds(30);

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
    @Test
    void verifyDeletingPolicyRevokesGrant(final @TempDir Path directory) throws Exception {
        val repository = new JsonAuthorizableResourceRepository(directory.toFile());
        try {
            repository.store(policyDocument("deleted"));
            repository.store(policyDocument("retained"));
            val engine = new DefaultAuthorizationEngine(repository, List.of(new DefaultResourceAuthorizer()));
            val request = AuthorizationRequest.builder().namespace("deleted").method("GET").uri("/protected")
                .principal(RegisteredServiceTestUtils.getPrincipal("alice", Map.of("role", List.of("reader")))).build();
            assertTrue(engine.authorize(request).getDecision());
            Files.delete(directory.resolve("deleted.json"));
            await().atMost(WATCHER_TIMEOUT).untilAsserted(() -> {
                assertFalse(engine.authorize(request).getDecision());
                assertTrue(repository.find("deleted", 1).isEmpty());
                assertEquals(1, repository.find("retained").size());
            });
        } finally {
            repository.destroy();
        }
    }

    @Test
    void verifyNamespaceRenameAndInvalidReplacement(final @TempDir Path directory) throws Exception {
        val repository = new JsonAuthorizableResourceRepository(directory.toFile());
        try {
            repository.store(policyDocument("before"));
            val file = directory.resolve("before.json");
            Files.writeString(file, Files.readString(file).replace("before", "after"));
            await().atMost(WATCHER_TIMEOUT).untilAsserted(() -> {
                assertTrue(repository.find("before").isEmpty());
                assertEquals(1, repository.find("after").size());
            });
            Files.writeString(file, "not valid json");
            await().atMost(WATCHER_TIMEOUT).until(() -> repository.find("after").isEmpty());
        } finally {
            repository.destroy();
        }
    }

    @Test
    void verifyNestedPolicyDeletionAndConflictingOwners(final @TempDir Path directory) throws Exception {
        val repository = new JsonAuthorizableResourceRepository(directory.toFile());
        try {
            repository.store(policyDocument("shared"));
            val original = directory.resolve("shared.json");
            val nested = Files.createDirectory(directory.resolve("nested"));
            val copy = nested.resolve("another.json");
            Files.copy(original, copy);
            await().atMost(WATCHER_TIMEOUT).until(() -> repository.find("shared").isEmpty());
            Files.delete(original);
            await().atMost(WATCHER_TIMEOUT).until(() -> repository.find("shared").size() == 1);
            Files.delete(copy);
            await().atMost(WATCHER_TIMEOUT).until(() -> repository.find("shared").isEmpty());
        } finally {
            repository.destroy();
        }
    }

    @Test
    void verifyAuthZenResourcesAcrossNamespaces(final @TempDir Path directory) throws Exception {
        val repository = new JsonAuthorizableResourceRepository(directory.toFile());
        try {
            repository.store(authZenDocument("first", "doc-[0-9]+"));
            repository.store(authZenDocument("second", null));
            val reloaded = new JsonAuthorizableResourceRepository(directory.toFile());
            try {
                val read = AuthZenAction.builder().name("can_read").build();
                val matching = AuthZenResource.builder().type("document").id("doc-1").build();
                assertEquals(2, reloaded.find(matching, read).size());
                assertEquals(1, reloaded.find(matching.withId("doc-1a"), read).size());
                assertTrue(reloaded.find(matching.withType("folder"), read).isEmpty());
                assertTrue(reloaded.find(matching, AuthZenAction.builder().name("can_delete").build()).isEmpty());
                val legacy = AuthorizationRequest.builder().namespace("first").method("GET").uri("/protected").build();
                assertTrue(reloaded.find(legacy).isEmpty());
            } finally {
                reloaded.destroy();
            }
        } finally {
            repository.destroy();
        }
    }

    @Test
    void verifyAuthZenResourceTypeIndexFollowsUpdates(final @TempDir Path directory) {
        val repository = new JsonAuthorizableResourceRepository(directory.toFile());
        try {
            val read = AuthZenAction.builder().name("can_read").build();
            val document = AuthZenResource.builder().type("document").id("doc-1").build();
            repository.store(authZenDocument("indexed", null));
            repository.store(policyDocument("untyped"));
            assertEquals(1, repository.find(document, read).size());

            val replacement = authZenDocument("indexed", null);
            replacement.getResources().getFirst().setResourceType("folder");
            repository.store(replacement);
            assertTrue(repository.find(document, read).isEmpty());
            assertEquals(1, repository.find(document.withType("folder"), read).size());
            assertTrue(repository.find(AuthZenResource.builder().id("doc-1").build(), read).isEmpty());
            assertTrue(repository.find(null, read).isEmpty());
        } finally {
            repository.destroy();
        }
    }

    private static AuthorizableResources authZenDocument(final String namespace, final String resourceIdPattern) {
        val document = new AuthorizableResources();
        document.setNamespace(namespace);
        document.getResources().add(new AuthorizableResource().setId(1).setResourceType("document")
            .setResourceIdPattern(resourceIdPattern == null ? null : Pattern.compile(resourceIdPattern))
            .setActions(Set.of("can_read"))
            .setPolicies(List.of(new RequiredAttributesAuthorizationPolicy(Map.of("role", Set.of("reader"))))));
        return document;
    }

    private static AuthorizableResources policyDocument(final String namespace) {
        val document = new AuthorizableResources();
        document.setNamespace(namespace);
        document.getResources().add(new AuthorizableResource().setId(1).setMethod("GET").setPattern(Pattern.compile("/protected"))
            .setPolicies(List.of(new RequiredAttributesAuthorizationPolicy(Map.of("role", Set.of("reader"))))));
        return document;
    }

}
