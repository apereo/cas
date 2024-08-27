package org.apereo.cas.web.view;

import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreWebAutoConfiguration.class)
class DynamicHtmlViewTests {
    @Autowired
    @Qualifier(CasWebflowConstants.VIEW_ID_DYNAMIC_HTML)
    private View dynamicHtmlView;

    @Test
    void verifyViewRendering() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        dynamicHtmlView.render(Map.of(DynamicHtmlView.class.getName(), "Hello"), request, response);
        assertEquals("Hello", response.getContentAsString());
    }

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val view = new DynamicHtmlView("<p>Hello</p>");
        assertEquals(MediaType.TEXT_HTML_VALUE, view.getContentType());
        assertDoesNotThrow(() -> view.render(Map.of(), request, response));
        assertNotNull(response.getContentAsString());
    }
}
