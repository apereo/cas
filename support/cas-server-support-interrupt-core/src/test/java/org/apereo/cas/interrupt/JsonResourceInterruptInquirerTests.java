package org.apereo.cas.interrupt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link JsonResourceInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonResourceInterruptInquirerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    
    @Test
    public void verifyResponseCanSerializeIntoJson() throws Exception {
        final Map<String, InterruptResponse> map = new LinkedHashMap<>();
        InterruptResponse response = new InterruptResponse("Message", 
                CollectionUtils.wrap("text", "link", "text2", "link2"), false, true);
        map.put("casuser", response);

        final File f = File.createTempFile("interrupt", "json");
        MAPPER.writer().withDefaultPrettyPrinter().writeValue(f, map);
        assertTrue(f.exists());
        
        final JsonResourceInterruptInquirer q = new JsonResourceInterruptInquirer(new FileSystemResource(f));
        response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"), CoreAuthenticationTestUtils.getRegisteredService(),
                CoreAuthenticationTestUtils.getService());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(response.getLinks().size(), 2);
    }
}
