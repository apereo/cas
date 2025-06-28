package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonResourceInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("FileSystem")
class JsonResourceInterruptInquirerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyResponse() throws Throwable {
        val map = new LinkedHashMap<String, InterruptResponse>();
        var response = new InterruptResponse("Message",
            CollectionUtils.wrap("text", "link", "text2", "link2"), false, true);
        response.setData(CollectionUtils.wrap("field1", List.of("value1", "value2"),
            "field2", List.of("value3", "value4")));
        map.put("casuser", response);

        val f = Files.createTempFile("interrupt", "json").toFile();
        MAPPER.writer().withDefaultPrettyPrinter().writeValue(f, map);
        assertTrue(f.exists());

        val inquirer = new JsonResourceInterruptInquirer(new FileSystemResource(f));
        response = inquirer.inquire(CoreAuthenticationTestUtils.getAuthentication("unknown"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new MockRequestContext());
        assertFalse(response.isInterrupt());

        response = inquirer.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new MockRequestContext());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(2, response.getLinks().size());
        assertTrue(response.getData().containsKey("field1"));
        assertTrue(response.getData().containsKey("field2"));

        inquirer.destroy();
    }
}
