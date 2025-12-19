package org.apereo.cas.configuration;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyNameException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test additional metadata validity.
 *
 * @since 6.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = AopAutoConfiguration.class)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class AdditionalMetadataVerificationTests {

    @Autowired
    private ResourceLoader resourceLoader;

    private static Set<ConfigurationMetadataProperty> getProperties(final Resource jsonFile) throws IOException {
        val builder = JsonMapper.builderWithJackson2Defaults().findAndAddModules();
        builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        builder.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        val mapper = builder.build();
        val jsonNodeRoot = mapper.readTree(jsonFile.getInputStream());
        val propertiesNode = jsonNodeRoot.get("properties");
        val values = new TypeReference<Set<ConfigurationMetadataProperty>>() {
        };
        val reader = mapper.readerFor(values);
        return reader.readValue(propertiesNode);
    }

    /**
     * Make sure the property names are canonical (not camel case) otherwise app won't start.
     * Spring boot will prevent startup if property names aren't valid.
     * It may be that some replacement properties need array syntax but none should contain [0].
     *
     * @throws IOException if additional property file is missing
     */
    @Test
    void verifyMetaData() throws IOException {
        val resource = CasConfigurationProperties.class.getClassLoader().getResource("META-INF/additional-spring-configuration-metadata.json");
        assertNotNull(resource);
        val additionalMetadataJsonFile = resourceLoader.getResource(resource.toString());
        val additionalProps = getProperties(additionalMetadataJsonFile);
        for (val prop : additionalProps) {
            try {
                ConfigurationPropertyName.of(prop.getName());
            } catch (final InvalidConfigurationPropertyNameException e) {
                fail(e::getMessage);
            }
            val deprecation = prop.getDeprecation();
            if (deprecation != null && StringUtils.isNotBlank(deprecation.getReplacement())) {
                ConfigurationPropertyName.of(deprecation.getReplacement());
                if (deprecation.getReplacement().endsWith("[0]")) {
                    /* array references may work, but not at the end. */
                    fail("Deprecation replacement should not end in [0].");
                }
            }
        }
    }
}
