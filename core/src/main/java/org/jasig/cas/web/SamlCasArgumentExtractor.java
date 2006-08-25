/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.RequestContext;

/**
 * Implementation of CasArgumentExtractor complaint with SAML protocol for
 * Browser/Artificat Profile.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SamlCasArgumentExtractor extends CasArgumentExtractor {

    /** Represent the SAML "destination site" */
    private static final String SAML_SERVICE_PARAMETER_NAME = "TARGET";

    /** Represents the SAML "artifact. */
    private static final String SAML_TICKET_PARAMETER_NAME = "SAMLart";

    public SamlCasArgumentExtractor(
        final CookieGenerator ticketGrantingTicketCookieGenerator,
        final CookieGenerator warnCookieGenerator) {
        super(ticketGrantingTicketCookieGenerator, warnCookieGenerator);
    }

    public String getServiceParameterName() {
        return SAML_SERVICE_PARAMETER_NAME;
    }

    public String getTicketParameterName() {
        return SAML_TICKET_PARAMETER_NAME;
    }

    public Service extractServiceFrom(final HttpServletRequest request) {
        if (!isServicePresent(request)) {
            return null;
        }

        final String strippedService = stripJsessionFromUrl(request
            .getParameter(getServiceParameterName()));

        try {
            return new SimpleService(URLEncoder
                .encode(strippedService, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public Service extractServiceFrom(final RequestContext context) {
        return extractServiceFrom(getHttpServletRequest(context));
    }
}
