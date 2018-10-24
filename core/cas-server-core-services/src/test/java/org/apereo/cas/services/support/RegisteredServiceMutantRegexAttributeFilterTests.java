package org.apereo.cas.services.support;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceMutantRegexAttributeFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RegisteredServiceMutantRegexAttributeFilterTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "RegisteredServiceMutantRegexAttributeFilterTests.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private RegisteredServiceMutantRegexAttributeFilter filter;

    @Mock
    private RegisteredService registeredService;

    private Map<String, Object> givenAttributesMap;

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.initMocks(this);
        when(this.registeredService.getName()).thenReturn("sample test service");
        when(this.registeredService.getServiceId()).thenReturn("https://www.apereo.org");
        this.filter = new RegisteredServiceMutantRegexAttributeFilter();

        this.givenAttributesMap = new HashMap<>();
        this.givenAttributesMap.put("employeeId", "E1234");
        this.givenAttributesMap.put("memberOf", Arrays.asList("math101", "science", "chemistry", "marathon101"));
    }

    @Test
    public void verifyPatternFilter() {
        this.filter.setPatterns(Collections.singletonMap("memberOf", "^m"));
        val attrs = this.filter.filter(this.givenAttributesMap);
        assertEquals(attrs.size(), this.givenAttributesMap.size());
        assertEquals(2, CollectionUtils.toCollection(attrs.get("memberOf")).size());
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
        this.filter.setPatterns(Collections.singletonMap("memberOf", CollectionUtils.wrapList("^mar(.+)", "^mat(.+)", "prefix$1")));
        this.filter.setExcludeUnmappedAttributes(true);
        this.filter.setCaseInsensitive(true);
        MAPPER.writeValue(JSON_FILE, this.filter);
        val filterRead = MAPPER.readValue(JSON_FILE, RegisteredServiceMutantRegexAttributeFilter.class);
        assertEquals(filter, filterRead);
    }

    @Test
    public void verifyMutantPatternValues() {
        this.filter.setPatterns(Collections.singletonMap("memberOf",
            CollectionUtils.wrapList("^mar(.+)(101) -> prefix$1$2",
                "^mat(.+)(101) -> postfix$1$2")));
        this.filter.setCaseInsensitive(false);
        this.filter.setExcludeUnmappedAttributes(true);
        val results = filter.filter(this.givenAttributesMap);
        assertEquals(1, results.size());
        val values = (Collection) results.get("memberOf");
        assertTrue(values.contains("prefixathon101"));
        assertTrue(values.contains("postfixh101"));
    }
}
