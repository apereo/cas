package org.apereo.cas.util;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Utility")
public class JsonUtilsTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    public void verifyRender() {
        val response = new MockHttpServletResponse();
        JsonUtils.render(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void verifyRenderException() {
        val response = new MockHttpServletResponse();
        JsonUtils.renderException(new RuntimeException("error"), response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void verifyRenderModel() throws Exception {
        val response = new MockHttpServletResponse();
        val model = Map.of("key", List.of("one", "two"));
        JsonUtils.render(model, response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        val content = response.getContentAsString();
        assertNotNull(MAPPER.readValue(content, Map.class));
    }
}
