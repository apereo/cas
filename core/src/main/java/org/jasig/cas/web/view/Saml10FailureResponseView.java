/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.web.CasArgumentExtractor;
import org.opensaml.SAMLException;
import org.opensaml.SAMLResponse;

/**
 * Represents a failed attempt at validating a ticket, responding via a SAML
 * assertion.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Saml10FailureResponseView extends AbstractCasView {

    private CasArgumentExtractor casArgumentExtractor;

    protected void renderMergedOutputModel(final Map model,
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {

        final SAMLResponse samlResponse = new SAMLResponse();
        samlResponse.setRecipient(this.casArgumentExtractor.extractServiceFrom(
            request).getId());
        samlResponse.setIssueInstant(new Date());
        samlResponse.setInResponseTo(this.casArgumentExtractor
            .extractTicketFrom(request));

        // XXX must include "Success", and not Description according to spec?
        final SAMLException samlException = new SAMLException("Success");

        samlResponse.setStatus(samlException);

        response.getWriter().print(samlResponse.toString());
    }

    public void setCasArgumentExtractor(
        final CasArgumentExtractor casArgumentExtractor) {
        this.casArgumentExtractor = casArgumentExtractor;
    }
}
