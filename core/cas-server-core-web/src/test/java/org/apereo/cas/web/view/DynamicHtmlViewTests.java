package org.apereo.cas.web.view;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamicHtmlViewTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class DynamicHtmlViewTests {
    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val view = new DynamicHtmlView("<p>Hello</p>");
        assertEquals(MediaType.TEXT_HTML_VALUE, view.getContentType());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                view.render(Map.of(), request, response);
            }
        });
        assertNotNull(response.getContentAsString());
    }
}
