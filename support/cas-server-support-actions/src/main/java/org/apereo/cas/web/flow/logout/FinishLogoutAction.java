package org.apereo.cas.web.flow.logout;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link FinishLogoutAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class FinishLogoutAction extends AbstractLogoutAction {
    @Override
    protected Event doInternalExecute(final HttpServletRequest request,
                                      final HttpServletResponse response,
                                      final RequestContext context) {
        val logoutRedirect = WebUtils.getLogoutRedirectUrl(context, String.class);
        if (StringUtils.isNotBlank(logoutRedirect)) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REDIRECT);
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_FINISH);
    }
}
