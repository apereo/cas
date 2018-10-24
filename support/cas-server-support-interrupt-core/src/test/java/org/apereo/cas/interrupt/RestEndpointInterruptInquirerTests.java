package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.configuration.model.support.interrupt.InterruptProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestEndpointInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(RestfulApiCategory.class)
public class RestEndpointInterruptInquirerTests {
    private MockWebServer webServer;

    @BeforeEach
    @SneakyThrows
    public void initialize() {
        val response = new InterruptResponse();
        response.setSsoEnabled(true);
        response.setInterrupt(true);
        response.setBlock(true);
        response.setMessage(getClass().getSimpleName());
        response.setLinks(CollectionUtils.wrap("text1", "link1", "text2", "link2"));

        val data = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writeValueAsString(response);
        this.webServer = new MockWebServer(8888,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        this.webServer.start();
    }

    @Test
    public void verifyResponseCanBeFoundFromRest() {
        val restProps = new InterruptProperties.Rest();
        restProps.setUrl("http://localhost:8888");
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(),
                new MockHttpServletRequest(),
                new MockHttpServletResponse()));
        val q = new RestEndpointInterruptInquirer(restProps);
        val response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            context);
        assertNotNull(response);
        assertTrue(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(2, response.getLinks().size());
        assertEquals(getClass().getSimpleName(), response.getMessage());
    }
}
