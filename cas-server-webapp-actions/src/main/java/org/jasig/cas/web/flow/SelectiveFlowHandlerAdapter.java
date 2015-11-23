package org.jasig.cas.web.flow;

import org.springframework.util.Assert;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;

import javax.validation.constraints.NotNull;
import java.util.Collections;
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
    @NotNull
    private Set<String> supportedFlowIds;

    public void setSupportedFlowIds(final Set<String> flowIdSet) {
        this.supportedFlowIds = flowIdSet;
    }

    public void setSupportedFlowId(final String flowId) {
        this.supportedFlowIds = Collections.singleton(flowId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.isTrue(!supportedFlowIds.isEmpty(), "Must specify at least one supported flow ID");
    }

    @Override
    public boolean supports(final Object handler) {
        return handler instanceof FlowHandler && supportedFlowIds.contains(((FlowHandler) handler).getFlowId());
    }
}
