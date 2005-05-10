/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.action.AbstractAction;

/**
 * Action for determining whether the warning page needs to be displayed or not.
 * If it does not need to be displayed we want to forward to the proper servce.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class WarnAction extends AbstractAction {

    protected Event doExecuteAction(RequestContext context) throws Exception {
        final HttpServletRequest request = ContextUtils.getHttpServletRequest(context);
        final boolean warn = Boolean.valueOf(
            WebUtils.getCookieValue(request, WebConstants.COOKIE_PRIVACY))
            .booleanValue();
        final boolean requestWarn = StringUtils.hasText(request
            .getParameter(WebConstants.WARN));

        if (warn || requestWarn) {
            return error();
        }
        // TODO: handle redirect automatically
        Map model = new HashMap();
        model.put(WebConstants.TICKET, ContextUtils.getAttribute(context, WebConstants.TICKET));
        ContextUtils.addAttribute(context, "model", model);
        return success();
    }
}
