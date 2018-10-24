package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.webflow.test.MockRequestContext;

import java.io.File;
import java.util.LinkedHashMap;

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
        val map = new LinkedHashMap<String, InterruptResponse>();
        var response = new InterruptResponse("Message",
            CollectionUtils.wrap("text", "link", "text2", "link2"), false, true);
        map.put("casuser", response);

        val f = File.createTempFile("interrupt", "json");
        MAPPER.writer().withDefaultPrettyPrinter().writeValue(f, map);
        assertTrue(f.exists());

        val q = new JsonResourceInterruptInquirer(new FileSystemResource(f));
        response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new MockRequestContext());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(2, response.getLinks().size());
    }
}
