package org.apereo.cas.util.scripting;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link GroovyScriptCacheManagerEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Groovy")
@TestPropertySource(properties = "management.endpoint.groovyCache.access=UNRESTRICTED")
@SpringBootTestAutoConfigurations
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyScriptCacheManagerEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier(ScriptResourceCacheManager.BEAN_NAME)
    private ScriptResourceCacheManager<String, ExecutableCompiledScript> scriptResourceCacheManager;

    @Test
    void verifyCompilation() throws Throwable {
        mockMvc.perform(post("/actuator/groovyCache/resources/validate")
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .content("groovy { return 1L}")
            )
            .andExpect(status().isOk());
        mockMvc.perform(post("/actuator/groovyCache/resources/validate")
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .content("println('Hello")
            )
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void verifyOperation() throws Throwable {
        val inlineScriptKey = UUID.randomUUID().toString();
        val classpathScriptKey = UUID.randomUUID().toString();
        scriptResourceCacheManager.resolveScriptableResource("groovy { return 1L}", inlineScriptKey);
        scriptResourceCacheManager.resolveScriptableResource("classpath:SampleScript.groovy", classpathScriptKey);
        assertEquals(2, scriptResourceCacheManager.getKeys().size());

        mockMvc.perform(get("/actuator/groovyCache/keys")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)));

        for (val key : scriptResourceCacheManager.getKeys()) {
            mockMvc.perform(get("/actuator/groovyCache/resources/{key}", key)
                    .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());
        }

        val computedClasspathKey = scriptResourceCacheManager.computeKey(classpathScriptKey);
        mockMvc.perform(post("/actuator/groovyCache/keys/{key}", computedClasspathKey))
            .andExpect(status().isOk());
        assertTrue(scriptResourceCacheManager.containsKey(computedClasspathKey));
        assertEquals(2, scriptResourceCacheManager.getKeys().size());

        val computedInlineKey = scriptResourceCacheManager.computeKey(inlineScriptKey);
        mockMvc.perform(post("/actuator/groovyCache/keys/{key}", computedInlineKey))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/actuator/groovyCache/keys/{key}", computedInlineKey))
            .andExpect(status().isOk());
        assertFalse(scriptResourceCacheManager.containsKey(computedInlineKey));
        assertEquals(1, scriptResourceCacheManager.getKeys().size());
    }
}
