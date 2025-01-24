package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAttributeDefinitionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = "attributeDefinitionStore", mode = ResourceAccessMode.READ_WRITE)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultAttributeDefinitionTests {

    private static AttributeDefinitionResolutionContext getAttributeDefinitionResolutionContext() {
        return AttributeDefinitionResolutionContext.builder()
            .attributeValues(List.of("v1", "v2"))
            .scope("example.org")
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .build();
    }

    @Test
    void verifyCaseCanonicalizationMode() throws Throwable {
        val key = "computedAttribute-%s".formatted(RandomUtils.randomAlphabetic(6));
        val defn = DefaultAttributeDefinition.builder()
            .key(key)
            .canonicalizationMode(CaseCanonicalizationMode.UPPER.name())
            .script("groovy { return ['value1', 'value2'] }")
            .build();

        val context = getAttributeDefinitionResolutionContext();

        val values = defn.resolveAttributeValues(context);
        assertTrue(values.contains("VALUE1"));
        assertTrue(values.contains("VALUE2"));
    }

    @Test
    void verifyNoCacheEmbeddedScriptOperation() throws Throwable {
        val key = "computedAttribute-%s".formatted(RandomUtils.randomAlphabetic(6));
        val defn = DefaultAttributeDefinition.builder()
            .key(key)
            .script("groovy { return ['hello world'] }")
            .build();
        val context = getAttributeDefinitionResolutionContext();
        val values = defn.resolveAttributeValues(context);
        assertFalse(values.isEmpty());
    }

    @Test
    void verifyBadScript() throws Throwable {
        val key = "computedAttribute-%s".formatted(RandomUtils.randomAlphabetic(6));
        val defn = DefaultAttributeDefinition.builder()
            .key(key)
            .script("badformat ()")
            .build();
        val context = getAttributeDefinitionResolutionContext();
        val values = defn.resolveAttributeValues(context);
        assertTrue(values.isEmpty());
    }

    @Test
    void verifyCachedEmbeddedScriptOperation() throws Throwable {
        val key = "computedAttribute-%s".formatted(RandomUtils.randomAlphabetic(6));
        val defn = DefaultAttributeDefinition.builder()
            .key(key)
            .script("groovy { return ['hello world'] }")
            .build();
        val context = getAttributeDefinitionResolutionContext();
        var values = defn.resolveAttributeValues(context);
        assertFalse(values.isEmpty());
        values = defn.resolveAttributeValues(context);
        assertFalse(values.isEmpty());
    }

    @Test
    void verifyCachedExternalScriptOperation() throws Throwable {
        val key = "computedAttribute-%s".formatted(RandomUtils.randomAlphabetic(6));
        val defn = DefaultAttributeDefinition.builder()
            .key(key)
            .script("classpath:ComputedAttributeDefinition.groovy")
            .build();
        val context = getAttributeDefinitionResolutionContext();
        var values = defn.resolveAttributeValues(context);
        assertFalse(values.isEmpty());
        values = defn.resolveAttributeValues(context);
        assertFalse(values.isEmpty());
    }

    @Test
    void verifyBadExternalScriptOperation() throws Throwable {
        val key = "computedAttribute-%s".formatted(RandomUtils.randomAlphabetic(6));
        val defn = DefaultAttributeDefinition.builder()
            .key(key)
            .script("classpath:BadScript.groovy")
            .build();
        val context = getAttributeDefinitionResolutionContext();
        val values = defn.resolveAttributeValues(context);
        assertTrue(values.isEmpty());
    }

    @Test
    void verifyBadEmbeddedScriptOperation() throws Throwable {
        val key = "computedAttribute-%s".formatted(RandomUtils.randomAlphabetic(6));
        val defn = DefaultAttributeDefinition.builder()
            .key(key)
            .script("groovy {xyz}")
            .build();
        val context = getAttributeDefinitionResolutionContext();
        val values = defn.resolveAttributeValues(context);
        assertTrue(values.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"MD5", "SHA1", "SHA256", "SHA512", "BASE64", "UNKNOWN"})
    void verifyHashingFunction(final String hashingStrategy) throws Throwable {
        val defn = DefaultAttributeDefinition.builder()
            .key("givenName")
            .hashingStrategy(hashingStrategy)
            .build();
        val context = getAttributeDefinitionResolutionContext();
        val values = defn.resolveAttributeValues(context);
        assertFalse(values.isEmpty());
    }
}
