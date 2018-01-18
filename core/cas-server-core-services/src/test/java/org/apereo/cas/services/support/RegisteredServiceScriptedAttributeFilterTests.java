package org.apereo.cas.services.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
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
@Slf4j
public class RegisteredServiceScriptedAttributeFilterTests {

    private Map<String, Object> givenAttributesMap;

    @Before
    public void setUp() {
        this.givenAttributesMap = new HashMap<>();
        this.givenAttributesMap.put("employeeId", "E1234");
        this.givenAttributesMap.put("memberOf", Arrays.asList("math101", "science", "chemistry", "marathon101"));
    }

    @Test
    public void verifyScriptedAttributeFilter() throws Exception {
        final RegisteredServiceScriptedAttributeFilter filter = new RegisteredServiceScriptedAttributeFilter();
        final File f = File.createTempFile("attr", ".groovy");
        final InputStream stream = new ClassPathResource("groovy-attr-filter.groovy").getInputStream();
        FileUtils.copyInputStreamToFile(stream, f);
        filter.setScript("file:" + f.getCanonicalPath());
        final Map results = filter.filter(this.givenAttributesMap);
        assertEquals(3, results.size());
    }
}
