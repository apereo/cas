package org.apereo.cas.services.support;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RegisteredServiceRegexAttributeFilterTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "registeredServiceRegexAttributeFilter.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String PHONE = "phone";

    private static final String FAMILY_NAME = "familyName";

    private static final String GIVEN_NAME = "givenName";

    private static final String UID = "uid";

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private final RegisteredServiceAttributeFilter filter;

    private final Map<String, List<Object>> givenAttributesMap;

    RegisteredServiceRegexAttributeFilterTests() {
        filter = new RegisteredServiceRegexAttributeFilter("^.{5,}$");

        givenAttributesMap = new HashMap<>();
        givenAttributesMap.put(UID, List.of("loggedInTestUid"));
        givenAttributesMap.put(PHONE, List.of("1290"));
        givenAttributesMap.put(FAMILY_NAME, List.of("Smith"));
        givenAttributesMap.put(GIVEN_NAME, List.of("John"));
        givenAttributesMap.put("employeeId", List.of("E1234"));
        givenAttributesMap.put("memberOf", Arrays.asList("math", "science", "chemistry"));
        givenAttributesMap.put("setAttribute", Stream.of("math", "science", "chemistry").collect(Collectors.toList()));
    }
    
    @Test
    void verifyPatternFilter() throws Throwable {
        val attrs = filter.filter(givenAttributesMap);
        assertEquals(5, attrs.size());

        assertFalse(attrs.containsKey(PHONE));
        assertFalse(attrs.containsKey(GIVEN_NAME));

        assertTrue(attrs.containsKey(UID));
        assertTrue(attrs.containsKey("memberOf"));

        val mapAttributes = attrs.get("setAttribute");
        assertTrue(mapAttributes.contains("science"));
        assertTrue(mapAttributes.contains("chemistry"));

        val obj = attrs.get("memberOf");
        assertEquals(2, obj.size());
    }

    @Test
    void verifyServiceAttributeFilterAllowedAttributesWithARegexFilter() throws Throwable {
        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("attr1", "attr3", "another"));
        policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter("v3"));
        val p = mock(Principal.class);

        val map = new HashMap<String, List<Object>>();
        map.put("attr1", List.of("value1"));
        map.put("attr2", List.of("value2"));
        map.put("attr3", Arrays.asList("v3", "v4"));

        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn("principalId");

        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("test"))
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(p)
            .build();
        val attr = policy.getAttributes(context);
        assertEquals(1, attr.size());
        assertTrue(attr.containsKey("attr3"));

        val data = SerializationUtils.serialize(policy);
        val p2 = SerializationUtils.deserializeAndCheckObject(data, ReturnAllowedAttributeReleasePolicy.class);
        assertNotNull(p2);
        assertEquals(p2.getAllowedAttributes(), policy.getAllowedAttributes());
        assertEquals(p2.getAttributeFilter(), policy.getAttributeFilter());
    }

    @Test
    void verifySerialization() {
        val data = SerializationUtils.serialize(filter);
        val secondFilter = SerializationUtils.deserializeAndCheckObject(data, RegisteredServiceAttributeFilter.class);
        assertEquals(secondFilter, filter);
    }

    @Test
    void verifyDefault() {
        val data = mock(RegisteredServiceAttributeFilter.class);
        when(data.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.HIGHEST_PRECEDENCE, data.getOrder());
    }

    @Test
    void verifySerializeARegisteredServiceRegexAttributeFilterToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, filter);
        val filterRead = MAPPER.readValue(JSON_FILE, RegisteredServiceRegexAttributeFilter.class);
        assertEquals(filter, filterRead);
    }
}
