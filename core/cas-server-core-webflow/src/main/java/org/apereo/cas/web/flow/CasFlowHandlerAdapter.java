package org.apereo.cas.web.flow;

import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;

/**
 * This is {@link CasFlowHandlerAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
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
}
