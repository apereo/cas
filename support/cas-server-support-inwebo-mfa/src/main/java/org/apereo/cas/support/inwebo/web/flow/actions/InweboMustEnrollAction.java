package org.apereo.cas.support.inwebo.web.flow.actions;

import module java.base;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A web action to enable the enrollment.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class InweboMustEnrollAction extends BaseCasWebflowAction {

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put(InweboWebflowConstants.MUST_ENROLL, true);
        WebUtils.addErrorMessageToContext(requestContext, "cas.inwebo.error.usernotregistered");
        return success();
    }
}
