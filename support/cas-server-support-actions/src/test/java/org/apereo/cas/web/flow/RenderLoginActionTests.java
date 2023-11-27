package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.Map;
import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

/**
 * This is {@link RenderLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowActions")
class RenderLoginActionTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Nested
    class DefaultTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM)
        private Action renderLoginAction;

        @Test
        void verifyNoRender() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            assertNull(renderLoginAction.execute(context));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.webflow.login-decorator.groovy.location=classpath:/GroovyLoginWebflowDecorator.groovy")
    class GroovyRendererTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM)
        private Action renderLoginAction;

        @Test
        void verifyGroovyRender() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            assertNull(renderLoginAction.execute(context));
            assertNotNull(context.getFlowScope().get("decoration"));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.webflow.login-decorator.rest.url=http://localhost:1234")
    class RestfulRendererTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM)
        private Action renderLoginAction;

        @Test
        void verifyRestfulRender() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val entity = MAPPER.writeValueAsString(Map.of("key", "value"));
            try (val webServer = new MockWebServer(1234,
                new ByteArrayResource(entity.getBytes(UTF_8), "Output"), OK)) {
                webServer.start();
                assertNull(renderLoginAction.execute(context));
                assertNotNull(context.getFlowScope().get("decoration"));
            }
        }
    }

}
