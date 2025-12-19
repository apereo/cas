package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AttributeBasedRegisteredServiceAccessStrategyActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class AttributeBasedRegisteredServiceAccessStrategyActivationCriteriaTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();
    
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySerializeToJson() throws Throwable {
        val criteria = new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria()
            .setOperator(LogicalOperatorTypes.OR)
            .setAllowIfInactive(true)
            .setRequiredAttributes(Map.of("common-name", List.of("n@m3"), "cn", List.of("***")));
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, criteria);
        val policyRead = MAPPER.readValue(jsonFile, RegisteredServiceAccessStrategyActivationCriteria.class);
        assertEquals(criteria, policyRead);
    }

    @Test
    void verifyRequiredPlain() {
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .principalId("casuser")
            .attributes(CollectionUtils.wrap("cn", List.of("name1")))
            .applicationContext(applicationContext)
            .build();
        val criteria = new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria()
            .setOperator(LogicalOperatorTypes.AND)
            .setAllowIfInactive(true)
            .setRequiredAttributes(Map.of("cn", List.of("name1", "name2")));
        assertTrue(criteria.isAllowIfInactive());
        assertTrue(criteria.shouldActivate(request));
    }

    @Test
    void verifyRequiredRegex() {
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .principalId("casuser")
            .attributes(CollectionUtils.wrap("cn", List.of("***")))
            .applicationContext(applicationContext)
            .build();
        val criteria = new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria()
            .setOperator(LogicalOperatorTypes.OR)
            .setAllowIfInactive(true)
            .setRequiredAttributes(Map.of("common-name", List.of("n@m3"), "cn", List.of("***")));
        assertTrue(criteria.shouldActivate(request));
    }
}
