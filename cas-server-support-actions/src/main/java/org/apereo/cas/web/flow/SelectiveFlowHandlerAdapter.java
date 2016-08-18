package org.apereo.cas.web.flow;

import org.springframework.util.Assert;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * Selective extension of {@link FlowHandlerAdapter} that only supports {@link FlowHandler}s matching one or
 * more flow IDs.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class SelectiveFlowHandlerAdapter extends FlowHandlerAdapter {

    /** List of supported flow IDs. */

    private Set<String> supportedFlowIds;

    public void setSupportedFlowIds(final Set<String> flowIdSet) {
        this.supportedFlowIds = flowIdSet;
    }

    /**
     * Sets supported flow id.
     *
     * @param flowId the flow id
     */
    public void setSupportedFlowId(final String flowId) {
        this.supportedFlowIds = new HashSet<>();
        this.supportedFlowIds.add(flowId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.isTrue(!this.supportedFlowIds.isEmpty(), "Must specify at least one supported flow ID");
    }

    @Override
    public boolean supports(final Object handler) {
        return handler instanceof FlowHandler && this.supportedFlowIds.contains(((FlowHandler) handler).getFlowId());
    }
}
