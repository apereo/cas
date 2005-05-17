/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.web.support.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller with the responsibility of handling redirects from the web flow
 * end state to the proper service.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class RedirectController implements Controller {

    public ModelAndView handleRequest(final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        final Map model = (Map) request.getAttribute("model");
        final String redirectUrl = (String) request
            .getAttribute(WebConstants.SERVICE);

        return new ModelAndView(new RedirectView(redirectUrl), model);
    }
}
