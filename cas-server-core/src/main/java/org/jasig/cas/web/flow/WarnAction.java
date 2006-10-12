/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.Assert;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
public final class WarnAction extends AbstractLoginAction {

    /** Event to publish in the scenario where a warning is required. */
    private static final String EVENT_WARN = "warn";

    /** Event to publish in the scenario when just a redirect is required. */
    private static final String EVENT_REDIRECT = "redirect";

    private CookieGenerator warnCookieGenerator;

    protected Event doExecute(final RequestContext context) throws Exception {
        return isWarnCookiePresent(context) ? result(EVENT_WARN)
            : result(EVENT_REDIRECT);
    }

    private boolean isWarnCookiePresent(final RequestContext context) {
        final HttpServletRequest request = WebUtils
            .getHttpServletRequest(context);
        final String value = WebUtils.getCookieValue(request,
            this.warnCookieGenerator.getCookieName());

        if (value == null) {
            return false;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    public void setWarnCookieGenerator(final CookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }

    protected void initActionInternal() throws Exception {
        Assert.notNull(this.warnCookieGenerator,
            "warnCookieGenerator cannot be null.");
    }
}
