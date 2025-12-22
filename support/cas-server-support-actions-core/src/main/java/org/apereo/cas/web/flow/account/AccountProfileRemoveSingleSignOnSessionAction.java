package org.apereo.cas.web.flow.account;

import module java.base;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccountProfileRemoveSingleSignOnSessionAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class AccountProfileRemoveSingleSignOnSessionAction extends BaseCasWebflowAction {
    private final SingleLogoutRequestExecutor singleLogoutRequestExecutor;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        val id = requestContext.getRequestParameters().get("id", String.class);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        singleLogoutRequestExecutor.execute(id, request, response);
        return tgt.equals(id) ? new Event(this, CasWebflowConstants.TRANSITION_ID_VALIDATE) : success();
    }
}
