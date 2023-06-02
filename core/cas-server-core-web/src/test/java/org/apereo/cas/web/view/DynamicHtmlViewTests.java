package org.apereo.cas.web.view;

import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamicHtmlViewTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Web")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class
})
public class DynamicHtmlViewTests {
    @Autowired
    @Qualifier(CasWebflowConstants.VIEW_ID_DYNAMIC_HTML)
    private View dynamicHtmlView;

    @Test
    public void verifyViewRendering() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        dynamicHtmlView.render(Map.of(DynamicHtmlView.class.getName(), "Hello"), request, response);
        assertEquals("Hello", response.getContentAsString());
    }

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val view = new DynamicHtmlView("<p>Hello</p>");
        assertEquals(MediaType.TEXT_HTML_VALUE, view.getContentType());
        assertDoesNotThrow(() -> view.render(Map.of(), request, response));
        assertNotNull(response.getContentAsString());
    }
}
