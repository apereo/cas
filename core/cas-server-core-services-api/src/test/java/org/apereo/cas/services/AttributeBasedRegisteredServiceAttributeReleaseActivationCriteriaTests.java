package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
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
 * This is {@link AttributeBasedRegisteredServiceAttributeReleaseActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("AttributeRelease")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class AttributeBasedRegisteredServiceAttributeReleaseActivationCriteriaTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySerializeToJson() throws Throwable {
        val policy = new ReturnMappedAttributeReleasePolicy();
        val criteria = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria()
            .setOperator(LogicalOperatorTypes.OR)
            .setRequiredAttributes(Map.of("common-name", List.of("n@m3"), "cn", List.of("***")));
        policy.setActivationCriteria(criteria);

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, policy);
        val policyRead = MAPPER.readValue(jsonFile, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policy, policyRead);
    }

    @Test
    void verifyRequiredRegex() throws Throwable {
        val policy = new ReturnMappedAttributeReleasePolicy();
        val mappedAttr = ArrayListMultimap.<String, Object>create();
        mappedAttr.put("cn", "common-name");
        policy.setAllowedAttributes(CollectionUtils.wrap(mappedAttr));

        val criteria = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria();
        criteria.setRequiredAttributes(Map.of("memberOf", List.of("ad.+|sys.+"), "unknown", List.of("unknown-value")));
        criteria.setOperator(LogicalOperatorTypes.OR);
        policy.setActivationCriteria(criteria);

        val attributes = getAttributesFromPolicy(policy);
        assertEquals(1, attributes.size());
        assertTrue(attributes.containsKey("common-name"));
    }

    @Test
    void verifyReverseMatch() throws Throwable {
        val policy = new ReturnMappedAttributeReleasePolicy();
        val mappedAttr = ArrayListMultimap.<String, Object>create();
        mappedAttr.put("cn", "common-name");
        policy.setAllowedAttributes(CollectionUtils.wrap(mappedAttr));

        val criteria = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria();
        criteria.setRequiredAttributes(Map.of("memberOf", List.of("ad.+|sys.+")));
        criteria.setReverseMatch(true);
        policy.setActivationCriteria(criteria);

        val attributes = getAttributesFromPolicy(policy);
        assertTrue(attributes.isEmpty());
    }

    @Test
    void verifyAllMustMatch() throws Throwable {
        val policy = new ReturnMappedAttributeReleasePolicy();
        val mappedAttr = ArrayListMultimap.<String, Object>create();
        mappedAttr.put("cn", "common-name");
        policy.setAllowedAttributes(CollectionUtils.wrap(mappedAttr));

        val criteria = new AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria();
        criteria.setRequiredAttributes(Map.of("memberOf", List.of("ad.+|sys.+"), "mail", List.of(".+@example.org")));
        criteria.setOperator(LogicalOperatorTypes.AND);
        policy.setActivationCriteria(criteria);

        val attributes = getAttributesFromPolicy(policy);
        assertTrue(attributes.containsKey("common-name"));
    }

    private Map<String, List<Object>> getAttributesFromPolicy(final ReturnMappedAttributeReleasePolicy policy) throws Throwable {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        return policy.getAttributes(context);
    }


}
