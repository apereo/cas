package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionRepositoryException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FlowExecutionExceptionResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Webflow")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class FlowExecutionExceptionResolverTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyActionNull() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val resolver = new FlowExecutionExceptionResolver();
        assertNull(resolver.resolveException(context.getHttpServletRequest(),
            context.getHttpServletResponse(), new Object(), new RuntimeException()));
    }

    @Test
    void verifyActionModelView() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRequestURI("/cas/login");
        context.setQueryString("param=value&something=something");
        val resolver = new FlowExecutionExceptionResolver();
        val mv = resolver.resolveException(context.getHttpServletRequest(), context.getHttpServletResponse(),
            new Object(), new ClientFlowExecutionRepositoryException("error"));
        assertNotNull(mv);
        assertTrue(mv.getModel().containsKey(resolver.getModelKey()));
    }
}
