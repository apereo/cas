/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.RequestContext;

/**
 * Action for determining whether the warning page needs to be displayed or not.
 * If it does not need to be displayed we want to forward to the proper service.
 * If there is a privacy request for a warning, the "warn" event is returned,
 * otherwise the "redirect" event is returned.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class WarnAction extends AbstractCasAction {

    /**
     * Event label for the event that will trigger the view state for warning
     * before going back to the service.
     */
    private static final String EVENT_WARN = "warn";

    /**
     * Event label for the event that will trigger the end state of being
     * redirected back to the service.
     */
    private static final String EVENT_REDIRECT = "redirect";

    protected ModelAndEvent doExecuteInternal(final RequestContext context,
        final Map attributes) throws Exception {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final boolean warn = Boolean.valueOf(
            WebUtils.getCookieValue(request, WebConstants.COOKIE_PRIVACY))
            .booleanValue();
        final boolean requestWarn = StringUtils.hasText(request
            .getParameter(WebConstants.WARN));

        if (warn || requestWarn) {
            return new ModelAndEvent(result(EVENT_WARN));
        }

        return new ModelAndEvent(result(EVENT_REDIRECT));
    }
}
