package org.apereo.cas.services.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RegisteredServiceMappedRegexAttributeFilterTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "registeredServiceMappedRegexAttributeFilter.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PHONE = "phone";
    private static final String FAMILY_NAME = "familyName";
    private static final String GIVEN_NAME = "givenName";
    private static final String UID = "uid";

    private final RegisteredServiceMappedRegexAttributeFilter filter;
    private final Map<String, Object> givenAttributesMap;

    @Mock
    private RegisteredService registeredService;

    public RegisteredServiceMappedRegexAttributeFilterTests() {
        this.filter = new RegisteredServiceMappedRegexAttributeFilter();

        this.givenAttributesMap = new HashMap<>();
        this.givenAttributesMap.put(UID, "loggedInTestUid");
        this.givenAttributesMap.put(PHONE, "1290");
        this.givenAttributesMap.put(FAMILY_NAME, "Smith");
        this.givenAttributesMap.put(GIVEN_NAME, "John");
        this.givenAttributesMap.put("employeeId", "E1234");
        this.givenAttributesMap.put("memberOf", Arrays.asList("math", "science", "chemistry", "marathon"));
        this.givenAttributesMap.put("arrayAttribute", new String[]{"math", "science", "chemistry"});
        this.givenAttributesMap.put("setAttribute", Stream.of("math", "science", "chemistry").collect(Collectors.toSet()));

        final Map<String, String> mapAttributes = new HashMap<>();
        mapAttributes.put(UID, "loggedInTestUid");
        mapAttributes.put(PHONE, "890");
        mapAttributes.put(FAMILY_NAME, "Smith");
        this.givenAttributesMap.put("mapAttribute", mapAttributes);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(this.registeredService.getName()).thenReturn("sample test service");
        when(this.registeredService.getServiceId()).thenReturn("https://www.jasig.org");
    }

    @Test
    public void verifyPatternFilter() {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^m"));
        final Map<String, Object> attrs = this.filter.filter(this.givenAttributesMap);
        assertEquals(attrs.size(), this.givenAttributesMap.size());
        assertEquals(CollectionUtils.toCollection(attrs.get("memberOf")).size(), 2);
    }

    @Test
    public void verifyPatternFilterExcludeUnmatched() {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^m"));
        this.filter.setExcludeUnmappedAttributes(true);
        final Map<String, Object> attrs = this.filter.filter(this.givenAttributesMap);
        assertEquals(attrs.size(), 1);
        assertEquals(CollectionUtils.toCollection(attrs.get("memberOf")).size(), 2);
    }

    @Test
    public void verifyPatternFilterFullMatch() {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^m"));
        this.filter.setCompleteMatch(true);
        final Map<String, Object> attrs = this.filter.filter(this.givenAttributesMap);
        assertEquals(attrs.size(), this.givenAttributesMap.size() - 1);
        assertFalse(attrs.containsKey("memberOf"));
    }

    @Test
    public void verifySerialization() {
        final byte[] data = SerializationUtils.serialize(this.filter);
        final RegisteredServiceAttributeFilter secondFilter =
                SerializationUtils.deserializeAndCheckObject(data, RegisteredServiceAttributeFilter.class);
        assertEquals(secondFilter, this.filter);
    }

    @Test
    public void verifySerializeARegisteredServiceRegexAttributeFilterToJson() throws IOException {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^\\w{3}$"));
        MAPPER.writeValue(JSON_FILE, this.filter);
        final RegisteredServiceAttributeFilter filterRead = MAPPER.readValue(JSON_FILE, RegisteredServiceMappedRegexAttributeFilter.class);
        assertEquals(filter, filterRead);
    }
}
