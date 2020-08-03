package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAttributeDefinitionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class DefaultAttributeDefinitionTests {

    @Test
    public void verifyNoCacheEmbeddedScriptOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val defn = DefaultAttributeDefinition.builder()
            .key("computedAttribute")
            .script("groovy { return ['hello world'] }")
            .build();
        val values = defn.resolveAttributeValues(List.of("v1", "v2"), "example.org",
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(values.isEmpty());
    }

    @Test
    public void verifyCachedEmbeddedScriptOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton(ApplicationContextProvider.BEAN_NAME_SCRIPT_RESOURCE_CACHE_MANAGER, GroovyScriptResourceCacheManager.class);
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val defn = DefaultAttributeDefinition.builder()
            .key("computedAttribute")
            .script("groovy { return ['hello world'] }")
            .build();
        var values = defn.resolveAttributeValues(List.of("v1", "v2"), "example.org",
            CoreAuthenticationTestUtils.getRegisteredService());
        assertFalse(values.isEmpty());
        values = defn.resolveAttributeValues(List.of("v1", "v2"), "example.org",
            CoreAuthenticationTestUtils.getRegisteredService());
        assertFalse(values.isEmpty());
    }

    @Test
    public void verifyNoCachedExternalScriptOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val defn = DefaultAttributeDefinition.builder()
            .key("computedAttribute")
            .script("classpath:ComputedAttributeDefinition.groovy")
            .build();
        val values = defn.resolveAttributeValues(List.of("v1", "v2"), "example.org",
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(values.isEmpty());
    }

    @Test
    public void verifyCachedExternalScriptOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton(ApplicationContextProvider.BEAN_NAME_SCRIPT_RESOURCE_CACHE_MANAGER, GroovyScriptResourceCacheManager.class);
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val defn = DefaultAttributeDefinition.builder()
            .key("computedAttribute")
            .script("classpath:ComputedAttributeDefinition.groovy")
            .build();
        var values = defn.resolveAttributeValues(List.of("v1", "v2"), "example.org",
            CoreAuthenticationTestUtils.getRegisteredService());
        assertFalse(values.isEmpty());
        values = defn.resolveAttributeValues(List.of("v1", "v2"), "example.org",
            CoreAuthenticationTestUtils.getRegisteredService());
        assertFalse(values.isEmpty());
    }

    @Test
    public void verifyBadExternalScriptOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton(ApplicationContextProvider.BEAN_NAME_SCRIPT_RESOURCE_CACHE_MANAGER, GroovyScriptResourceCacheManager.class);
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val defn = DefaultAttributeDefinition.builder()
            .key("computedAttribute")
            .script("classpath:BadScript.groovy")
            .build();
        val values = defn.resolveAttributeValues(List.of("v1", "v2"), "example.org",
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(values.isEmpty());
    }

    @Test
    public void verifyBadEmbeddedScriptOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton(ApplicationContextProvider.BEAN_NAME_SCRIPT_RESOURCE_CACHE_MANAGER, GroovyScriptResourceCacheManager.class);
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val defn = DefaultAttributeDefinition.builder()
            .key("computedAttribute")
            .script("groovy {xyz}")
            .build();
        val values = defn.resolveAttributeValues(List.of("v1", "v2"), "example.org",
            CoreAuthenticationTestUtils.getRegisteredService());
        assertNull(values);
    }


}
