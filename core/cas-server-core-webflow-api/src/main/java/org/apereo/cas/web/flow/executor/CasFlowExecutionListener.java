package org.apereo.cas.web.flow.executor;

import lombok.RequiredArgsConstructor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;

/**
 * This is {@link CasFlowExecutionListener}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class CasFlowExecutionListener implements FlowExecutionListener {
    private final CasConfigurationProperties casProperties;

    @Override
    public void viewRendering(final RequestContext context, final View view, final StateDefinition viewState) {
        injectIntoRequestContext(context);
    }

    @Override
    public void sessionStarting(final RequestContext context, final FlowSession session, final MutableAttributeMap<?> input) {
        injectIntoRequestContext(context);
    }

    @Override
    public void requestSubmitted(final RequestContext context) {
        injectIntoRequestContext(context);
    }

    private void injectIntoRequestContext(final RequestContext context) {
        context.getConversationScope().put("casProperties", casProperties);
    }
}
