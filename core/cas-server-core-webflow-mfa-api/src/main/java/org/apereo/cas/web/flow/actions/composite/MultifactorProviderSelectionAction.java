package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.val;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorProviderSelectionAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class MultifactorProviderSelectionAction extends BaseCasWebflowAction {
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val conditions = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MultifactorProviderSelectionCriteria.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .toList();
        val eventId = conditions.isEmpty() || conditions.stream().allMatch(criteria -> criteria.shouldProceedWithMultifactorProviderSelection(requestContext))
            ? CasWebflowConstants.TRANSITION_ID_PROCEED
            : CasWebflowConstants.TRANSITION_ID_SELECT;
        val attributes = requestContext.getCurrentEvent().getAttributes();
        return eventFactory.event(this, eventId, attributes);
    }
}
