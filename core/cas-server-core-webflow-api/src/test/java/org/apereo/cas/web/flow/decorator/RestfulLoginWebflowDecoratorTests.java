package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.configuration.model.webapp.WebflowLoginDecoratorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestfulLoginWebflowDecoratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Category(RestfulApiCategory.class)
public class RestfulLoginWebflowDecoratorTests {
    @Test
    public void verifyOperation() {
        val props = new WebflowLoginDecoratorProperties.Rest();
        props.setUrl("http://localhost:9465");

        val rest = new RestfulLoginWebflowDecorator(props);
        val requestContext = new MockRequestContext();

        try (val webServer = new MockWebServer(9465,
            new ByteArrayResource(getJsonData().getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            rest.decorate(requestContext, mock(ApplicationContext.class));
            assertTrue(requestContext.getFlowScope().contains("decoration"));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @SneakyThrows
    private static String getJsonData() {
        return new ObjectMapper().writeValueAsString(CollectionUtils.wrap("key", "value"));
    }
}
