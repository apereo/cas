package org.apereo.cas.web.flow;

import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
        val plan = getApplicationContext().getBean(CasWebflowExecutionPlan.BEAN_NAME, CasWebflowExecutionPlan.class);
        return plan.isInitialized()
            ? super.handle(request, response, handler)
            : new ModelAndView(CasWebflowConstants.VIEW_ID_ERROR, HttpStatusCode.valueOf(HttpStatus.SC_TOO_EARLY));
    }
}
