/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.validation.Assertion;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Custom View to Return the CAS 1.0 Protocol Response. Implemented as a view
 * class rather than a JSP (like CAS 2.0 spec) because of the requirement of the
 * line feeds to be "\n".
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas10ResponseView extends AbstractView {

    /**
     * Indicate whether this view will be generating the success response or
     * not.
     */
    private boolean successResponse;

    protected void renderMergedOutputModel(final Map model,
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final Assertion assertion = (Assertion) model
            .get(WebConstants.ASSERTION);

        if (this.successResponse) {
            response.getWriter().print(
                "yes\n"
                    + (assertion.getChainedAuthentications()[0]).getPrincipal()
                        .getId() + "\n");
        } else {
            response.getWriter().print("no\n\n");
        }
    }

    public void setSuccessResponse(final boolean successResponse) {
        this.successResponse = successResponse;
    }
}
