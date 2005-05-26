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
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class WarnAction extends AbstractCasAction {

    protected ModelAndEvent doExecuteInternal(final RequestContext context, final Map attributes)
        throws Exception {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final boolean warn = Boolean.valueOf(
            WebUtils.getCookieValue(request, WebConstants.COOKIE_PRIVACY))
            .booleanValue();
        final boolean requestWarn = StringUtils.hasText(request
            .getParameter(WebConstants.WARN));

        if (warn || requestWarn) {
            return new ModelAndEvent(error());
        }

        return new ModelAndEvent(success());
    }
}
