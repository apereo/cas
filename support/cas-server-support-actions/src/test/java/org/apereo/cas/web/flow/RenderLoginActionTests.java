package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.login.RenderLoginAction;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;

/**
 * This is {@link RenderLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowActions")
public class RenderLoginActionTests extends AbstractWebflowActionsTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyNoRender() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val properties = new CasConfigurationProperties();
        val action = new RenderLoginAction(getServicesManager(), properties, applicationContext);
        assertNull(action.execute(context));
    }

    @Test
    public void verifyGroovyRender() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val properties = new CasConfigurationProperties();
        properties.getWebflow().getLoginDecorator()
            .getGroovy().setLocation(new ClassPathResource("GroovyLoginDecorator.groovy"));
        val action = new RenderLoginAction(getServicesManager(), properties, applicationContext);
        assertNull(action.execute(context));
    }

    @Test
    public void verifyRestfulRender() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val entity = MAPPER.writeValueAsString(Map.of("key", "value"));
        try (val webServer = new MockWebServer(1234,
            new ByteArrayResource(entity.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();

            val properties = new CasConfigurationProperties();
            properties.getWebflow().getLoginDecorator().getRest().setUrl("http://localhost:1234");
            val action = new RenderLoginAction(getServicesManager(), properties, applicationContext);
            assertNull(action.execute(context));
        }
    }
}
