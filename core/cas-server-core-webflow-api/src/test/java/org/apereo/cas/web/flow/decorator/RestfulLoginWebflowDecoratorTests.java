package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.configuration.model.core.web.flow.RestfulWebflowLoginDecoratorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestfulLoginWebflowDecoratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApi")
class RestfulLoginWebflowDecoratorTests {
    private static String getJsonData() throws Exception {
        return new ObjectMapper().writeValueAsString(CollectionUtils.wrap("key", "value"));
    }

    @Test
    void verifyOperation() throws Throwable {
        try (val webServer = new MockWebServer(getJsonData())) {
            webServer.start();
            val props = new RestfulWebflowLoginDecoratorProperties();
            props.setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val rest = new RestfulLoginWebflowDecorator(props);
            val requestContext = MockRequestContext.create(mock(ApplicationContext.class));
            rest.decorate(requestContext);
            assertTrue(requestContext.getFlowScope().contains("decoration"));
        }
    }
}
