package org.apereo.cas.services.support;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class RegisteredServiceRegexAttributeFilterTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "registeredServiceRegexAttributeFilter.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final String PHONE = "phone";
    private static final String FAMILY_NAME = "familyName";
    private static final String GIVEN_NAME = "givenName";
    private static final String UID = "uid";

    private final RegisteredServiceAttributeFilter filter;
    private final Map<String, List<Object>> givenAttributesMap;

    @Mock
    private RegisteredService registeredService;

    public RegisteredServiceRegexAttributeFilterTests() {
        this.filter = new RegisteredServiceRegexAttributeFilter("^.{5,}$");

        this.givenAttributesMap = new HashMap<>();
        this.givenAttributesMap.put(UID, List.of("loggedInTestUid"));
        this.givenAttributesMap.put(PHONE, List.of("1290"));
        this.givenAttributesMap.put(FAMILY_NAME, List.of("Smith"));
        this.givenAttributesMap.put(GIVEN_NAME, List.of("John"));
        this.givenAttributesMap.put("employeeId", List.of("E1234"));
        this.givenAttributesMap.put("memberOf", Arrays.asList("math", "science", "chemistry"));
        this.givenAttributesMap.put("setAttribute", Stream.of("math", "science", "chemistry").collect(Collectors.toList()));
    }

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.initMocks(this);

        when(this.registeredService.getName()).thenReturn("sample test service");
        when(this.registeredService.getServiceId()).thenReturn("https://www.jasig.org");
    }

    @Test
    public void verifyPatternFilter() {

        val attrs = this.filter.filter(this.givenAttributesMap);
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
    public void verifyServiceAttributeFilterAllowedAttributesWithARegexFilter() {
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

        val attr = policy.getAttributes(p, RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getRegisteredService("test"));
        assertEquals(1, attr.size());
        assertTrue(attr.containsKey("attr3"));

        val data = SerializationUtils.serialize(policy);
        val p2 = SerializationUtils.deserializeAndCheckObject(data, ReturnAllowedAttributeReleasePolicy.class);
        assertNotNull(p2);
        assertEquals(p2.getAllowedAttributes(), policy.getAllowedAttributes());
        assertEquals(p2.getAttributeFilter(), policy.getAttributeFilter());
    }

    @Test
    public void verifySerialization() {
        val data = SerializationUtils.serialize(this.filter);
        val secondFilter =SerializationUtils.deserializeAndCheckObject(data, RegisteredServiceAttributeFilter.class);
        assertEquals(secondFilter, this.filter);
    }

    @Test
    public void verifySerializeARegisteredServiceRegexAttributeFilterToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, filter);
        val filterRead = MAPPER.readValue(JSON_FILE, RegisteredServiceRegexAttributeFilter.class);
        assertEquals(filter, filterRead);
    }
}
