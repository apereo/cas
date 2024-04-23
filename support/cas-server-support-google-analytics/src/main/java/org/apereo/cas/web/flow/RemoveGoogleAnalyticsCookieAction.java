package org.apereo.cas.web.flow;

import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RemoveGoogleAnalyticsCookieAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class RemoveGoogleAnalyticsCookieAction extends BaseCasWebflowAction {
    private final CasCookieBuilder googleAnalyticsCookieBuilder;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        googleAnalyticsCookieBuilder.removeCookie(response);
        return null;
    }
}
