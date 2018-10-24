package org.apereo.cas.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyNameException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test additional metadata validity.
 *
 * @since 6.0
 */
@Slf4j
public class AdditionalMetadataVerificationTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Make sure the property names are canonical (not camel case) otherwise app won't start.
     * Spring boot {@link org.springframework.boot.context.properties.migrator.PropertiesMigrationListener}
     * will prevent startup if property names aren't valid.
     * It may be that some replacement properties need array syntax but none should contain [0].
     * @throws IOException if additional property file is missing
     */
    @Test
    public void verifyMetaData() throws IOException {
        val additionalMetadataJsonFile = resourceLoader.getResource("META-INF/additional-spring-configuration-metadata.json");
        val additionalProps = getProperties(additionalMetadataJsonFile);
        for (val prop : additionalProps) {
            try {
                ConfigurationPropertyName.of(prop.getName());
            } catch (final InvalidConfigurationPropertyNameException e) {
                fail(e.getMessage());
            }
            val deprecation = prop.getDeprecation();
            if (deprecation != null && StringUtils.isNotBlank(deprecation.getReplacement())) {
                ConfigurationPropertyName.of(deprecation.getReplacement());
                if (deprecation.getReplacement().endsWith("[0]")) {
                    // array references may work, but not at the end
                    fail("Deprecation replacement should not end in [0].");
                }
            }
        }
    }

    private Set<ConfigurationMetadataProperty> getProperties(final Resource jsonFile) throws IOException {
        val mapper = new ObjectMapper().findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        val jsonNodeRoot = mapper.readTree(jsonFile.getURL());
        val propertiesNode = jsonNodeRoot.get("properties");
        val values = new TypeReference<Set<ConfigurationMetadataProperty>>() {
        };
        val reader = mapper.readerFor(values);
        val jsonSet = (Set) reader.readValue(propertiesNode);
        return jsonSet;
    }
}
