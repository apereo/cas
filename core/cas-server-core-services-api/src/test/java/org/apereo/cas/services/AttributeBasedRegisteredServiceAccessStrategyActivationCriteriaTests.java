package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AttributeBasedRegisteredServiceAccessStrategyActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
public class AttributeBasedRegisteredServiceAccessStrategyActivationCriteriaTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(),
        "AttributeBasedRegisteredServiceAccessStrategyActivationCriteriaTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();


    @Test
    public void verifySerializeToJson() throws Exception {
        val criteria = new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria()
            .setOperator(LogicalOperatorTypes.OR)
            .setAllowIfInactive(true)
            .setRequiredAttributes(Map.of("common-name", List.of("n@m3"), "cn", List.of("***")));
        MAPPER.writeValue(JSON_FILE, criteria);
        val policyRead = MAPPER.readValue(JSON_FILE, RegisteredServiceAccessStrategyActivationCriteria.class);
        assertEquals(criteria, policyRead);
    }

    @Test
    public void verifyRequiredPlain() {
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .principalId("casuser")
            .attributes(CollectionUtils.wrap("cn", List.of("name1")))
            .build();
        val criteria = new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria()
            .setOperator(LogicalOperatorTypes.AND)
            .setAllowIfInactive(true)
            .setRequiredAttributes(Map.of("cn", List.of("name1", "name2")));
        assertTrue(criteria.isAllowIfInactive());
        assertTrue(criteria.shouldActivate(request));
    }

    @Test
    public void verifyRequiredRegex() {
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .principalId("casuser")
            .attributes(CollectionUtils.wrap("cn", List.of("***")))
            .build();
        val criteria = new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria()
            .setOperator(LogicalOperatorTypes.OR)
            .setAllowIfInactive(true)
            .setRequiredAttributes(Map.of("common-name", List.of("n@m3"), "cn", List.of("***")));
        assertTrue(criteria.shouldActivate(request));
    }
}
