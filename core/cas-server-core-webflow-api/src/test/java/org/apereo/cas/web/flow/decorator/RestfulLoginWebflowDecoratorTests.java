package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.configuration.model.core.web.flow.RestfulWebflowLoginDecoratorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestfulLoginWebflowDecoratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApi")
public class RestfulLoginWebflowDecoratorTests {
    @Test
    public void verifyOperation() {
        val props = new RestfulWebflowLoginDecoratorProperties();
        props.setUrl("http://localhost:9465");

        val rest = new RestfulLoginWebflowDecorator(props);
        val requestContext = new MockRequestContext();

        try (val webServer = new MockWebServer(9465,
            new ByteArrayResource(getJsonData().getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            rest.decorate(requestContext, mock(ApplicationContext.class));
            assertTrue(requestContext.getFlowScope().contains("decoration"));
        }
    }

    @SneakyThrows
    private static String getJsonData() {
        return new ObjectMapper().writeValueAsString(CollectionUtils.wrap("key", "value"));
    }
}
