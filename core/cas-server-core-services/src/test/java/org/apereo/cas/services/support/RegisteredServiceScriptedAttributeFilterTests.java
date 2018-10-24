package org.apereo.cas.services.support;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link RegisteredServiceScriptedAttributeFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RegisteredServiceScriptedAttributeFilterTests {

    private Map<String, Object> givenAttributesMap;

    @BeforeEach
    public void initialize() {
        this.givenAttributesMap = new HashMap<>();
        this.givenAttributesMap.put("employeeId", "E1234");
        this.givenAttributesMap.put("memberOf", Arrays.asList("math101", "science", "chemistry", "marathon101"));
    }

    @Test
    public void verifyScriptedAttributeFilter() throws Exception {
        val filter = new RegisteredServiceScriptedAttributeFilter();
        val f = File.createTempFile("attr", ".groovy");
        val stream = new ClassPathResource("groovy-attr-filter.groovy").getInputStream();
        FileUtils.copyInputStreamToFile(stream, f);
        filter.setScript("file:" + f.getCanonicalPath());
        val results = filter.filter(this.givenAttributesMap);
        assertEquals(3, results.size());
    }
}
