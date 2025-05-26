package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.function.Consumer;

/**
 * This is {@link ConsumerExecutionAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Accessors(chain = true)
public class ConsumerExecutionAction extends BaseCasWebflowAction {
    /**
     * Consumer action that does nothing and returns null, effectively being a no-op.
     */
    public static final Action NONE = new ConsumerExecutionAction(ctx -> {
    });

    /**
     * Consumer action that sets the response status to {@link HttpStatus#OK}
     * and marks the response as completed.
     */
    public static final Action OK = new ConsumerExecutionAction(ctx -> {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(ctx);
        response.setStatus(HttpStatus.OK.value());
        ctx.getExternalContext().recordResponseComplete();
    });

    /**
     * Consumer action that sets the response status to {@link HttpStatus#NO_CONTENT}
     * and marks the response as completed.
     */
    public static final Action NO_CONTENT = new ConsumerExecutionAction(ctx -> {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(ctx);
        response.setStatus(HttpStatus.NO_CONTENT.value());
        ctx.getExternalContext().recordResponseComplete();
    });

    /**
     * Consumer action that populates the flow scope with the current event attributes.
     */
    public static final Action EVENT_ATTRIBUTES_TO_FLOW_SCOPE = new ConsumerExecutionAction(ctx -> {
        if (ctx.getCurrentEvent() != null) {
            ctx.getFlowScope().putAll(ctx.getCurrentEvent().getAttributes());
        }
    });

    private final Consumer<RequestContext> task;

    @Setter
    private String eventId;

    @Override
    public Event doExecuteInternal(final RequestContext requestContext) {
        this.task.accept(requestContext);
        return StringUtils.isNotBlank(this.eventId) ? eventFactory.event(this, this.eventId) : null;
    }

    @Override
    public String toString() {
        return "InlineActionReturns%s".formatted(StringUtils.defaultIfBlank(this.eventId, "None"));
    }

    /**
     * Wrap action.
     *
     * @param innerAction the inner action
     * @return the action
     */
    public static Action wrap(final Action innerAction) {
        return new ConsumerExecutionAction(Unchecked.consumer(innerAction::execute));
    }
}
