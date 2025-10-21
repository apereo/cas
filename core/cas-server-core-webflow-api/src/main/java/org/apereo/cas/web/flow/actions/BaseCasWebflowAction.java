package org.apereo.cas.web.flow.actions;

import org.apereo.cas.support.events.web.flow.CasWebflowActionExecutedEvent;
import org.apereo.cas.support.events.web.flow.CasWebflowActionExecutingEvent;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.decorator.WebflowDecorator;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.execution.ActionExecutionException;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link BaseCasWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public abstract class BaseCasWebflowAction extends AbstractAction {
    protected final EventFactorySupport eventFactory = new EventFactorySupport();
    
    /**
     * Is login flow active.
     *
     * @param requestContext the request context
     * @return true/false
     */
    protected static boolean isLoginFlowActive(final RequestContext requestContext) {
        val currentFlowId = Optional.ofNullable(requestContext.getActiveFlow())
            .map(FlowDefinition::getId).orElse("unknown");
        return currentFlowId.equalsIgnoreCase(CasWebflowConfigurer.FLOW_ID_LOGIN);
    }

    @Override
    protected Event doPreExecute(final RequestContext requestContext) throws Exception {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        FunctionUtils.doIfNotNull(applicationContext, _ ->
            BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, WebflowDecorator.class)
                .values()
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .forEach(Unchecked.consumer(decorator -> decorator.decorate(requestContext))));
        return super.doPreExecute(requestContext);
    }

    @Override
    protected final Event doExecute(final RequestContext requestContext) throws Exception {
        val activeFlow = requestContext.getActiveFlow();
        try {
            WebUtils.putActiveFlow(requestContext);
            val applicationContext = activeFlow.getApplicationContext();
            val clientInfo = ClientInfoHolder.getClientInfo();
            val scope = new HashMap<>(requestContext.getConversationScope().asMap());
            scope.putAll(requestContext.getFlowScope().asMap());
            scope.putAll(requestContext.getFlashScope().asMap());
            applicationContext.publishEvent(new CasWebflowActionExecutingEvent(this, scope, clientInfo));
            return doExecuteInternal(requestContext);
        } catch (final Exception e) {
            throw e;
        } catch (final Throwable e) {
            val currentState = Optional.ofNullable(requestContext.getCurrentState())
                .map(StateDefinition::getId).orElse("unknown");
            throw new ActionExecutionException(activeFlow.getId(),
                currentState, this, requestContext.getAttributes(), e);
        }
    }

    @Override
    protected void doPostExecute(final RequestContext requestContext) {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val clientInfo = ClientInfoHolder.getClientInfo();
        val scope = new HashMap<>(requestContext.getConversationScope().asMap());
        scope.putAll(requestContext.getFlowScope().asMap());
        scope.putAll(requestContext.getFlashScope().asMap());
        applicationContext.publishEvent(new CasWebflowActionExecutedEvent(this, scope, clientInfo));
    }

    protected abstract Event doExecuteInternal(RequestContext requestContext) throws Throwable;
}
