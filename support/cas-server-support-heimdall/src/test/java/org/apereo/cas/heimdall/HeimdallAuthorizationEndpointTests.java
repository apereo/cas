package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResources;
import org.apereo.cas.heimdall.authorizer.resource.policy.RequiredAttributesAuthorizationPolicy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link HeimdallAuthorizationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Authorization")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = BaseHeimdallTests.SharedTestConfiguration.class,
    properties = {
        "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.heimdall.access=UNRESTRICTED",
        "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/heimdalloidc.jwks",
        "cas.heimdall.json.location=classpath:/policies"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class HeimdallAuthorizationEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOperation() throws Exception {
        val authzRequest = AuthorizationRequest.builder()
            .uri("/api/claims")
            .method("PUT")
            .namespace("API_CLAIMS")
            .build()
            .toJson();
        mockMvc.perform(get("/actuator/heimdall/resource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authzRequest))
            .andExpect(status().isOk());
    }
    
    @Test
    void verifyAllResources() throws Exception {
        mockMvc.perform(get("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        
        mockMvc.perform(get("/actuator/heimdall/resources/API_CLAIMS")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/heimdall/resources/API_CLAIMS/1453626")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void verifyCreateResource() throws Exception {
        val resource = new AuthorizableResource();
        resource.setId(9001);
        resource.setPattern(Pattern.compile("/api/tests/create"));
        resource.setMethod("GET");

        val resources = new AuthorizableResources();
        resources.setNamespace("API_TESTS_CREATE");
        resources.getResources().add(resource);

        mockMvc.perform(post("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(resources)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.namespace").value("API_TESTS_CREATE"))
            .andExpect(jsonPath("$.resources[0].id").value(9001))
            .andExpect(jsonPath("$.resources[0].pattern").value("/api/tests/create"))
            .andExpect(jsonPath("$.resources[0].method").value("GET"));
    }

    @Test
    void verifyCreateResourceWithMultipleResources() throws Exception {
        val first = new AuthorizableResource();
        first.setId(9201);
        first.setPattern(Pattern.compile("/api/tests/multi/one"));
        first.setMethod("GET");

        val second = new AuthorizableResource();
        second.setId(9202);
        second.setPattern(Pattern.compile("/api/tests/multi/two"));
        second.setMethod("POST");

        val resources = new AuthorizableResources();
        resources.setNamespace("API_TESTS_MULTI");
        resources.getResources().add(first);
        resources.getResources().add(second);

        mockMvc.perform(post("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(resources)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resources.length()").value(2))
            .andExpect(jsonPath("$.resources[0].id").value(9201))
            .andExpect(jsonPath("$.resources[1].id").value(9202));
    }

    @Test
    void verifyCreateResourceWithPolicies() throws Exception {
        val resource = new AuthorizableResource();
        resource.setId(9301);
        resource.setPattern(Pattern.compile("/api/tests/policies"));
        resource.setMethod("GET");
        resource.getPolicies().add(new RequiredAttributesAuthorizationPolicy(Map.of("iss", Set.of("https://issuer.example.org"))));

        val resources = new AuthorizableResources();
        resources.setNamespace("API_TESTS_POLICIES");
        resources.getResources().add(resource);

        mockMvc.perform(post("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(resources)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resources[0].policies.length()").value(1))
            .andExpect(jsonPath("$.resources[0].policies[0].attributes.iss[0]").value("https://issuer.example.org"));
    }

    @Test
    void verifyCreateResourcePersistsToDisk() throws Exception {
        val resource = new AuthorizableResource();
        resource.setId(9101);
        resource.setPattern(Pattern.compile("/api/tests/persist"));
        resource.setMethod("DELETE");

        val resources = new AuthorizableResources();
        resources.setNamespace("API_TESTS_PERSIST");
        resources.getResources().add(resource);

        mockMvc.perform(post("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(resources)))
            .andExpect(status().isOk());

        val directory = casProperties.getHeimdall().getJson().getLocation().getFile();
        val jsonFile = new File(directory, "API_TESTS_PERSIST.json");
        assertTrue(jsonFile.exists());
        val contents = Files.readString(jsonFile.toPath());
        assertTrue(contents.contains("API_TESTS_PERSIST"));
        assertTrue(contents.contains("/api/tests/persist"));
    }

    @Test
    void verifyCreateResourceOverwritesExistingNamespace() throws Exception {
        val namespace = "API_TESTS_OVERWRITE";

        val original = new AuthorizableResource();
        original.setId(9401);
        original.setPattern(Pattern.compile("/api/tests/overwrite/original"));
        original.setMethod("GET");
        val originalResources = new AuthorizableResources();
        originalResources.setNamespace(namespace);
        originalResources.getResources().add(original);

        mockMvc.perform(post("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(originalResources)))
            .andExpect(status().isOk());

        val replacement = new AuthorizableResource();
        replacement.setId(9402);
        replacement.setPattern(Pattern.compile("/api/tests/overwrite/replacement"));
        replacement.setMethod("PUT");
        val replacementResources = new AuthorizableResources();
        replacementResources.setNamespace(namespace);
        replacementResources.getResources().add(replacement);

        mockMvc.perform(post("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(replacementResources)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resources.length()").value(1))
            .andExpect(jsonPath("$.resources[0].id").value(9402));

        val directory = casProperties.getHeimdall().getJson().getLocation().getFile();
        val jsonFile = new File(directory, namespace + ".json");
        val contents = Files.readString(jsonFile.toPath());
        assertFalse(contents.contains("overwrite/original"));
        assertTrue(contents.contains("overwrite/replacement"));
    }

    @Test
    void verifyCreatedResourceIsImmediatelyDiscoverable() throws Exception {
        val namespace = "API_TESTS_ROUNDTRIP";

        val resource = new AuthorizableResource();
        resource.setId(9501);
        resource.setPattern(Pattern.compile("/api/tests/roundtrip"));
        resource.setMethod("GET");

        val resources = new AuthorizableResources();
        resources.setNamespace(namespace);
        resources.getResources().add(resource);

        mockMvc.perform(post("/actuator/heimdall/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(resources)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/heimdall/resources/" + namespace)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(9501));

        mockMvc.perform(get("/actuator/heimdall/resources/" + namespace + "/9501")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pattern").value("/api/tests/roundtrip"));

        val authzRequest = AuthorizationRequest.builder()
            .uri("/api/tests/roundtrip")
            .method("GET")
            .namespace(namespace)
            .build()
            .toJson();
        mockMvc.perform(get("/actuator/heimdall/resource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authzRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(9501));
    }

    @Test
    void verifyNotFound() throws Exception {
        val authzRequest = AuthorizationRequest.builder()
            .uri("/api/claims")
            .method("OPTIONS")
            .namespace("API_CLAIMS")
            .build()
            .toJson();
        mockMvc.perform(get("/actuator/heimdall/resource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authzRequest))
            .andExpect(status().isNotFound());
    }
}
