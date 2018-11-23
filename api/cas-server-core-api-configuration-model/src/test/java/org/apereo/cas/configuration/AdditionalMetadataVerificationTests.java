package org.apereo.cas.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyNameException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import lombok.val;

import static org.junit.Assert.*;

public class AdditionalMetadataVerificationTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Make sure the property names are canonical (not camel case) otherwise app won't start.
     * Spring boot PropertiesMigrationListener will prevent startup if property names aren't valid.
     * @throws IOException if additional property file is missing
     */
    @Test
    public void verifyMetaData() throws IOException {
        val mapper = new ObjectMapper().findAndRegisterModules();
        val jsonFile = resourceLoader.getResource("file:src/main/resources/META-INF/additional-spring-configuration-metadata.json");
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        val values = new TypeReference<Map<String, Set<ConfigurationMetadataProperty>>>() {
        };
        final Map<String, Set<ConfigurationMetadataProperty>> jsonMap = mapper.readValue(jsonFile.getURL(), values);
        final Set<ConfigurationMetadataProperty> props = jsonMap.get("properties");
        for (val prop: props) {
            try {
                ConfigurationPropertyName.of(prop.getName());
            } catch (final InvalidConfigurationPropertyNameException e) {
                fail(e.getMessage());
            }
        }
    }
}
