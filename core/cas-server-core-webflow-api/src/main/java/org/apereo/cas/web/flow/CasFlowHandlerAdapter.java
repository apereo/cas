package org.apereo.cas.web.flow;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class CasFlowHandlerAdapter extends FlowHandlerAdapter {
    private final String supportedFlowId;
    private final CasWebflowExecutionPlan webflowExecutionPlan;
    
    @Override
    public boolean supports(final Object handler) {
        return super.supports(handler) && ((FlowHandler) handler).getFlowId().equals(supportedFlowId);
    }

    @Override
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        if (!webflowExecutionPlan.isInitialized()) {
            LOGGER.debug("Configuring CAS webflow execution plan...");
            webflowExecutionPlan.execute();
            request.setAttribute(CasWebflowExecutionPlan.class.getName(), webflowExecutionPlan.isInitialized());
        }
        return super.handle(request, response, handler);
    }
}
