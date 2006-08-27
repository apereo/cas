/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.web.support.SamlArgumentExtractor;
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

    private final SamlArgumentExtractor samlArgumentExtractor = new SamlArgumentExtractor();

    protected void renderMergedOutputModel(final Map model,
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final Service service = this.samlArgumentExtractor.extractService(
            request);

        final SAMLResponse samlResponse = new SAMLResponse(
            this.samlArgumentExtractor.extractTicketArtifact(request), service != null ? service.getId() : null, new ArrayList(), new SAMLException("Success"));
        samlResponse.setIssueInstant(new Date());

        response.getWriter().print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        response.setContentType("text/xml");
        response.getWriter().print(samlResponse.toString());
    }
}
