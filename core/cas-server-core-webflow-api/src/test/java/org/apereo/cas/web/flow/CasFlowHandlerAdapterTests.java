package org.apereo.cas.web.flow;

import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpMethod;
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
class CasFlowHandlerAdapterTests {
    @Test
    void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val plan = mock(CasWebflowExecutionPlan.class);
        when(plan.isInitialized()).thenReturn(false);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, plan, CasWebflowExecutionPlan.BEAN_NAME);
        val adapter = new CasFlowHandlerAdapter("login", plan);
        adapter.setApplicationContext(applicationContext);
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        assertThrows(RuntimeException.class, () -> adapter.handle(request, new MockHttpServletResponse(), mock(FlowHandler.class)));
        assertNotNull(request.getAttribute(CasWebflowExecutionPlan.class.getName()));
    }
}
