package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingRegisteredServiceAttributeReleaseActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ChainingRegisteredServiceAttributeReleaseActivationCriteriaTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySerializeToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val chain = new ChainingRegisteredServiceAttributeReleaseActivationCriteria().setOperator(LogicalOperatorTypes.AND);
        val criteria1 = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria()
            .setRequiredAttributes(Map.of("cn", List.of("name1", "name2")));
        val criteria2 = new GroovyRegisteredServiceAttributeReleaseActivationCriteria()
            .setGroovyScript("groovy { return false }");
        chain.addConditions(criteria1, criteria2);
        MAPPER.writeValue(jsonFile, chain);
        val policyRead = MAPPER.readValue(jsonFile, ChainingRegisteredServiceAttributeReleaseActivationCriteria.class);
        assertEquals(chain, policyRead);
    }

    @Test
    void verifyPolicyWithAND() {
        val chain = new ChainingRegisteredServiceAttributeReleaseActivationCriteria().setOperator(LogicalOperatorTypes.AND);
        val criteria1 = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria()
            .setRequiredAttributes(Map.of("memberOf", List.of("sports")));
        val criteria2 = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria()
            .setRequiredAttributes(Map.of("givenName", List.of("test")));
        chain.addConditions(criteria1, criteria2);
        assertFalse(getAttributesFromPolicy(chain));
    }

    @Test
    void verifyPolicyWithOR() {
        val chain = new ChainingRegisteredServiceAttributeReleaseActivationCriteria().setOperator(LogicalOperatorTypes.OR);
        val criteria2 = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria()
            .setRequiredAttributes(Map.of("memberOf", List.of("staff")));
        val criteria1 = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria()
            .setRequiredAttributes(Map.of("givenName", List.of("unknown")));
        chain.addConditions(criteria1, criteria2);
        assertTrue(getAttributesFromPolicy(chain));
    }

    private boolean getAttributesFromPolicy(final RegisteredServiceAttributeReleaseActivationCriteria policy) {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        return policy.shouldActivate(context);
    }
}
