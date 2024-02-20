package org.apereo.cas.web.flow;

import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasFlowHandlerAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Webflow")
public class CasFlowHandlerAdapterTests {
    @Test
    void verifyOperation() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val plan = mock(CasWebflowExecutionPlan.class);
        when(plan.isInitialized()).thenReturn(false);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, plan, CasWebflowExecutionPlan.BEAN_NAME);
        val adapter = new CasFlowHandlerAdapter("login");
        adapter.setApplicationContext(applicationContext);
        val mv = adapter.handle(new MockHttpServletRequest(), new MockHttpServletResponse(), mock(FlowHandler.class));
        assertNotNull(mv);
        assertEquals(HttpStatus.TOO_EARLY.value(), mv.getStatus().value());
    }
}
