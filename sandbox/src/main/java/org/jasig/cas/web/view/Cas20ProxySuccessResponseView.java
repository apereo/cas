/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.io.PrintWriter;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlOptions;

import org.jasig.cas.web.support.WebConstants;
import org.springframework.web.servlet.View;

import edu.yale.tp.cas.ServiceResponseDocument;
import edu.yale.tp.cas.ServiceResponseType;
import edu.yale.tp.cas.ProxySuccessType;

/**
 * <p>
 * CAS 2.0 proxy success response view, implemented in XMLBeans.
 * </p>
 * 
 * @author Drew Mazurek
 * @version $Revision$ $Date$
 * @since 3.0.1
 */
public class Cas20ProxySuccessResponseView extends AbstractCas20ResponseView
    implements View {

    // get our superclass's default xmlOptions object
    private final XmlOptions xmlOptions = super.getXmlOptions();

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.View#render(java.util.Map,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public final void render(final Map model, final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {

        final ServiceResponseDocument responseDoc = ServiceResponseDocument.Factory
            .newInstance(this.xmlOptions);
        final ServiceResponseType serviceResponse = responseDoc
            .addNewServiceResponse();
        final ProxySuccessType proxySuccess = serviceResponse
            .addNewProxySuccess();

        proxySuccess.setProxyTicket((String) model.get(WebConstants.TICKET));

        response.setContentType(super.getHttpContentType() + "; charset="
            + super.getHttpCharset());
        final PrintWriter out = response.getWriter();
        responseDoc.save(out, this.xmlOptions);
    }
}
