package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasFlowHandlerAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class CasFlowHandlerAdapter extends FlowHandlerAdapter {
    private final String supportedFlowId;

    public CasFlowHandlerAdapter(final String supportedFlowId) {
        this.supportedFlowId = supportedFlowId;
        setUseCacheControlHeader(false);
    }

    @Override
    public boolean supports(final Object handler) {
        return super.supports(handler) && ((FlowHandler) handler).getFlowId().equals(supportedFlowId);
    }

    @Override
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        val webflowExecutionPlan = getApplicationContext().getBean(CasWebflowExecutionPlan.BEAN_NAME, CasWebflowExecutionPlan.class);
        if (!webflowExecutionPlan.isInitialized()) {
            LOGGER.debug("Configuring CAS webflow execution plan...");
            webflowExecutionPlan.execute();
            request.setAttribute(CasWebflowExecutionPlan.class.getName(), webflowExecutionPlan.isInitialized());
        }
        return super.handle(request, response, handler);
    }
}
