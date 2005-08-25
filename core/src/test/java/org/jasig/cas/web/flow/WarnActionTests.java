/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.TestUtils;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class WarnActionTests extends TestCase {

    private WarnAction warnAction = new WarnAction();

    public void testWarnFromCookie() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie[] {new Cookie(
            WebConstants.COOKIE_PRIVACY,
            WebConstants.COOKIE_DEFAULT_FILLED_VALUE)});

        assertEquals("warn", this.warnAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testWarnFromRequestParameter() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.WARN, "true");

        assertEquals("redirect", this.warnAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testNoWarn() throws Exception {
        assertEquals("redirect", this.warnAction.doExecute(
            TestUtils.getContext()).getId());
    }
}
