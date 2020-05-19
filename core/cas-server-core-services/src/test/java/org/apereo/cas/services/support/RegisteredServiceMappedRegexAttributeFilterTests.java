package org.apereo.cas.services.support;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("Simple")
public class RegisteredServiceMappedRegexAttributeFilterTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "registeredServiceMappedRegexAttributeFilter.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final String PHONE = "phone";

    private static final String FAMILY_NAME = "familyName";

    private static final String GIVEN_NAME = "givenName";

    private static final String UID = "uid";

    private final Map<String, List<Object>> givenAttributesMap;

    private RegisteredServiceMappedRegexAttributeFilter filter;

    @Mock
    private RegisteredService registeredService;

    public RegisteredServiceMappedRegexAttributeFilterTests() {
        givenAttributesMap = new HashMap<>();
        givenAttributesMap.put(UID, List.of("loggedInTestUid"));
        givenAttributesMap.put(PHONE, List.of("1290"));
        givenAttributesMap.put(FAMILY_NAME, List.of("Smith"));
        givenAttributesMap.put(GIVEN_NAME, List.of("John"));
        givenAttributesMap.put("employeeId", List.of("E1234"));
        givenAttributesMap.put("memberOf", Arrays.asList("math", "science", "chemistry", "marathon"));
        givenAttributesMap.put("setAttribute", Stream.of("math", "science", "chemistry").collect(Collectors.toList()));
    }

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.initMocks(this);
        this.filter = new RegisteredServiceMappedRegexAttributeFilter();
        when(this.registeredService.getName()).thenReturn("sample test service");
        when(this.registeredService.getServiceId()).thenReturn("https://www.jasig.org");
    }

    @Test
    public void verifyPatternFilter() {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^m"));
        val attrs = this.filter.filter(givenAttributesMap);
        assertEquals(attrs.size(), givenAttributesMap.size());
        assertEquals(2, CollectionUtils.toCollection(attrs.get("memberOf")).size());
    }

    @Test
    public void verifyPattern() {
        this.filter = new RegisteredServiceMappedRegexAttributeFilter(Collections.singletonMap("memberOf", "^m"));
        val attrs = this.filter.filter(givenAttributesMap);
        assertEquals(attrs.size(), givenAttributesMap.size());
        assertEquals(2, CollectionUtils.toCollection(attrs.get("memberOf")).size());
    }

    @Test
    public void verifyPatternFilterExcludeUnmatched() {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^m"));
        this.filter.setExcludeUnmappedAttributes(true);
        val attrs = this.filter.filter(givenAttributesMap);
        assertEquals(1, attrs.size());
        assertEquals(2, CollectionUtils.toCollection(attrs.get("memberOf")).size());
    }

    @Test
    public void verifyPatternFilterFullMatch() {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^m"));
        this.filter.setCompleteMatch(true);
        val attrs = this.filter.filter(givenAttributesMap);
        assertEquals(attrs.size(), givenAttributesMap.size() - 1);
        assertFalse(attrs.containsKey("memberOf"));
    }

    @Test
    public void verifySerialization() {
        val data = SerializationUtils.serialize(this.filter);
        val secondFilter =
            SerializationUtils.deserializeAndCheckObject(data, RegisteredServiceAttributeFilter.class);
        assertEquals(secondFilter, this.filter);
    }

    @Test
    public void verifySerializeARegisteredServiceRegexAttributeFilterToJson() throws IOException {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^\\w{3}$"));
        MAPPER.writeValue(JSON_FILE, this.filter);
        val filterRead = MAPPER.readValue(JSON_FILE, RegisteredServiceMappedRegexAttributeFilter.class);
        assertEquals(filter, filterRead);
    }
}
