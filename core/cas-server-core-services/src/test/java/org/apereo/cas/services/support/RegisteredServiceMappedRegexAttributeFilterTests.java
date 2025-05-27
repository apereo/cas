package org.apereo.cas.services.support;

import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("RegisteredService")
class RegisteredServiceMappedRegexAttributeFilterTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "registeredServiceMappedRegexAttributeFilter.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String PHONE = "phone";

    private static final String FAMILY_NAME = "familyName";

    private static final String GIVEN_NAME = "givenName";

    private static final String UID = "uid";

    private final Map<String, List<Object>> givenAttributesMap;

    private RegisteredServiceMappedRegexAttributeFilter filter;

    RegisteredServiceMappedRegexAttributeFilterTests() {
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
    void initialize() {
        this.filter = new RegisteredServiceMappedRegexAttributeFilter();
    }

    @Test
    void verifyPatternFilter() {
        this.filter.setPatterns(Map.of("memberOf", "^m"));
        val attrs = this.filter.filter(givenAttributesMap);
        assertEquals(attrs.size(), givenAttributesMap.size());
        assertEquals(2, CollectionUtils.toCollection(attrs.get("memberOf")).size());
    }

    @Test
    void verifyPattern() {
        this.filter = new RegisteredServiceMappedRegexAttributeFilter(Map.of("memberOf", "^m"));
        val attrs = this.filter.filter(givenAttributesMap);
        assertEquals(attrs.size(), givenAttributesMap.size());
        assertEquals(2, CollectionUtils.toCollection(attrs.get("memberOf")).size());
    }

    @Test
    void verifyPatternFilterExcludeUnmatched() {
        this.filter.setPatterns(Map.of("memberOf", "^m"));
        this.filter.setExcludeUnmappedAttributes(true);
        val attrs = this.filter.filter(givenAttributesMap);
        assertEquals(1, attrs.size());
        assertEquals(2, CollectionUtils.toCollection(attrs.get("memberOf")).size());
    }

    @Test
    void verifyPatternFilterFullMatch() {
        this.filter.setPatterns(Map.of("memberOf", "^m"));
        this.filter.setCompleteMatch(true);
        val attrs = this.filter.filter(givenAttributesMap);
        assertEquals(attrs.size(), givenAttributesMap.size() - 1);
        assertFalse(attrs.containsKey("memberOf"));
    }

    @Test
    void verifySerialization() {
        val data = SerializationUtils.serialize(this.filter);
        val secondFilter = SerializationUtils.deserializeAndCheckObject(data, RegisteredServiceAttributeFilter.class);
        assertEquals(secondFilter, this.filter);
    }

    @Test
    void verifySerializeARegisteredServiceRegexAttributeFilterToJson() throws IOException {
        this.filter.setPatterns(Map.of("memberOf", "^\\w{3}$"));
        MAPPER.writeValue(JSON_FILE, this.filter);
        val filterRead = MAPPER.readValue(JSON_FILE, RegisteredServiceMappedRegexAttributeFilter.class);
        assertEquals(filter, filterRead);
    }
}
