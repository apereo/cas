/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.web.servlet.View;

/**
 * Custom View to Return the CAS 1.0 Protocol Response. Implemented as a view
 * class rather than a JSP (like CAS 2.0 spec) because of the requirement of the
 * line feeds to be "\n".
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas10ResponseView implements View {

    private boolean successResponse;

    public void render(Map model, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        Assertion assertion = (Assertion) model.get(WebConstants.ASSERTION);

        if (this.successResponse) {
            response.getWriter().write(
                "yes\n"
                    + ((Authentication) assertion.getChainedAuthentications()
                        .get(0)).getPrincipal().getId() + "\n");
        } else {
            response.getWriter().write("no\n\n");
        }
        
        response.flushBuffer();
    }

    public void setSuccessResponse(boolean successResponse) {
        this.successResponse = successResponse;
    }
}
