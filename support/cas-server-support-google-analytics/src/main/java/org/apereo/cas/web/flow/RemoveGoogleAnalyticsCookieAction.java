package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RemoveGoogleAnalyticsCookieAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class RemoveGoogleAnalyticsCookieAction extends AbstractAction {
    private final CasConfigurationProperties casProperties;

    private final CasCookieBuilder googleAnalyticsCookieBuilder;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        googleAnalyticsCookieBuilder.removeCookie(response);
        return null;
    }
}
