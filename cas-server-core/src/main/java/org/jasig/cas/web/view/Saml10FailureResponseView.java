/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.WebApplicationService;
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

    private static final String DEFAULT_ENCODING = "UTF-8";

    private final SamlArgumentExtractor samlArgumentExtractor = new SamlArgumentExtractor();

    @NotNull
    private String encoding = DEFAULT_ENCODING;

    protected void renderMergedOutputModel(final Map model,
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final WebApplicationService service = this.samlArgumentExtractor.extractService(request);
        final String artifactId = service != null ? service.getArtifactId() : null;
        final String serviceId = service != null ? service.getId() : "UNKNOWN";
        final String errorMessage = (String) model.get("description");
        final SAMLResponse samlResponse = new SAMLResponse(artifactId, serviceId, new ArrayList<Object>(), new SAMLException(errorMessage));
        samlResponse.setIssueInstant(new Date());

        response.setContentType("text/xml; charset=" + this.encoding);
        response.getWriter().print("<?xml version=\"1.0\" encoding=\"" + this.encoding + "\"?>");
        response.getWriter().print("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body>");
        response.getWriter().print(samlResponse.toString());
        response.getWriter().print("</SOAP-ENV:Body></SOAP-ENV:Envelope>");
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }
}
