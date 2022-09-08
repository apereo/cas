package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingRegisteredServiceAccessStrategyActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
public class ChainingRegisteredServiceAccessStrategyActivationCriteriaTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(),
        "ChainingRegisteredServiceAccessStrategyActivationCriteriaTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();


    @Test
    public void verifyOrOperation() {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build();

        val chain = new ChainingRegisteredServiceAccessStrategyActivationCriteria();
        chain.addConditions(RegisteredServiceAccessStrategyActivationCriteria.always(),
            RegisteredServiceAccessStrategyActivationCriteria.never());
        chain.setOperator(RegisteredServiceChainOperatorTypes.OR);
        assertTrue(chain.shouldActivate(request));
        assertTrue(chain.shouldAllowIfInactive());
    }

    @Test
    public void verifyAndOperation() {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build();
        val chain = new ChainingRegisteredServiceAccessStrategyActivationCriteria();
        chain.addConditions(RegisteredServiceAccessStrategyActivationCriteria.always(),
            RegisteredServiceAccessStrategyActivationCriteria.never());
        chain.setOperator(RegisteredServiceChainOperatorTypes.AND);
        assertFalse(chain.shouldActivate(request));
        assertTrue(chain.shouldAllowIfInactive());
    }

    @Test
    public void verifySerializeToJson() throws Exception {
        val chain = new ChainingRegisteredServiceAccessStrategyActivationCriteria();
        chain.addConditions(new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria(),
            new GroovyRegisteredServiceAccessStrategyActivationCriteria());
        chain.setOperator(RegisteredServiceChainOperatorTypes.AND);
        MAPPER.writeValue(JSON_FILE, chain);
        val policyRead = MAPPER.readValue(JSON_FILE, RegisteredServiceAccessStrategyActivationCriteria.class);
        assertEquals(chain, policyRead);
    }
}
