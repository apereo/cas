package org.apereo.cas.web.flow.actions;

import module java.base;
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
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.execution.ActionExecutionException;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
        val applicationContext = activeFlow.getApplicationContext();
        val transactionManager = getTransactionManager(requestContext);
        val transactionStatus = getTransaction(transactionManager);

        try {
            WebUtils.putActiveFlow(requestContext);
            val clientInfo = ClientInfoHolder.getClientInfo();
            val scope = buildScopeMap(requestContext);
            applicationContext.publishEvent(new CasWebflowActionExecutingEvent(this, scope, clientInfo));
            val result = doExecuteInternal(requestContext);
            transactionManager.ifPresent(mgr -> transactionStatus.ifPresent(mgr::commit));
            return result;
        } catch (final Exception e) {
            transactionManager.ifPresent(mgr -> transactionStatus.ifPresent(mgr::rollback));
            throw e;
        } catch (final Throwable e) {
            transactionManager.ifPresent(mgr -> transactionStatus.ifPresent(mgr::rollback));
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
        val scope = buildScopeMap(requestContext);
        applicationContext.publishEvent(new CasWebflowActionExecutedEvent(this, scope, clientInfo));
    }

    protected abstract @Nullable Event doExecuteInternal(RequestContext requestContext) throws Throwable;

    protected Optional<TransactionStatus> getTransaction(
        final Optional<PlatformTransactionManager> transactionManager) {
        return transactionManager
            .map(manager -> {
                val def = new DefaultTransactionDefinition();
                def.setName(getClass().getSimpleName() + "-TransactionID-" + UUID.randomUUID());
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                return manager.getTransaction(def);
            });
    }

    protected Optional<PlatformTransactionManager> getTransactionManager(final RequestContext requestContext) {
        val annotation = AnnotationUtils.findAnnotation(getClass(), Transactional.class);
        return FunctionUtils.doIfNotNull(annotation, () -> {
            val activeFlow = requestContext.getActiveFlow();
            val applicationContext = activeFlow.getApplicationContext();
            val transactionManagerName = annotation.transactionManager();
            return Optional.of(applicationContext.getBean(transactionManagerName, PlatformTransactionManager.class));
        }, Optional::<PlatformTransactionManager>empty).get();
    }

    protected Map<String, Object> buildScopeMap(final RequestContext requestContext) {
        val conversationScope = requestContext.getConversationScope().asMap();
        val flowScope = requestContext.getFlowScope().asMap();
        val flashScope = requestContext.getFlashScope().asMap();
        val totalSize = conversationScope.size() + flowScope.size() + flashScope.size();
        val scope = new HashMap<String, Object>(totalSize);
        scope.putAll(conversationScope);
        scope.putAll(flowScope);
        scope.putAll(flashScope);
        return scope;
    }
}
