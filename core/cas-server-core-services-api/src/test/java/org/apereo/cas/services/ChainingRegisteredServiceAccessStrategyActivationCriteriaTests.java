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
 * This is {@link ChainingRegisteredServiceAccessStrategyActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class ChainingRegisteredServiceAccessStrategyActivationCriteriaTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOrOperation() {
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .principalId("casuser")
            .attributes(CollectionUtils.wrap("key1", Set.of("value1")))
            .applicationContext(applicationContext)
            .build();

        val chain = new ChainingRegisteredServiceAccessStrategyActivationCriteria();
        chain.addConditions(RegisteredServiceAccessStrategyActivationCriteria.always(),
            RegisteredServiceAccessStrategyActivationCriteria.never());
        chain.setOperator(LogicalOperatorTypes.OR);
        assertTrue(chain.shouldActivate(request));
        assertTrue(chain.isAllowIfInactive());
    }

    @Test
    void verifyAndOperation() {
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .applicationContext(applicationContext)
            .principalId("casuser")
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build();
        val chain = new ChainingRegisteredServiceAccessStrategyActivationCriteria();
        chain.addConditions(RegisteredServiceAccessStrategyActivationCriteria.always(),
            RegisteredServiceAccessStrategyActivationCriteria.never());
        chain.setOperator(LogicalOperatorTypes.AND);
        assertFalse(chain.shouldActivate(request));
        assertTrue(chain.isAllowIfInactive());
    }

    @Test
    void verifySerializeToJson() throws Throwable {
        val chain = new ChainingRegisteredServiceAccessStrategyActivationCriteria();

        val criteria1 = new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria()
            .setOrder(1)
            .setOperator(LogicalOperatorTypes.AND)
            .setAllowIfInactive(true)
            .setRequiredAttributes(Map.of("cn", List.of("name1", "name2")));
        val criteria2 = new GroovyRegisteredServiceAccessStrategyActivationCriteria()
            .setOrder(2)
            .setGroovyScript("groovy { return false }");
        chain.addConditions(criteria1, criteria2);
        chain.setOperator(LogicalOperatorTypes.AND);

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, chain);
        val policyRead = MAPPER.readValue(jsonFile, RegisteredServiceAccessStrategyActivationCriteria.class);
        assertEquals(chain, policyRead);
    }
}
