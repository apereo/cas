/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

/**
 * Class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SamlService extends AbstractWebApplicationService {

    /** Constant representing service. */
    private static final String CONST_PARAM_SERVICE = "TARGET";

    /** Constant representing artifact. */
    private static final String CONST_PARAM_TICKET = "SAMLart";

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -6867572626767140223L;

    public SamlService(final String id) {
        super(id, id, null);
    }

    protected SamlService(final String id, final String originalUrl,
        final String artifactId) {
        super(id, originalUrl, artifactId);
    }

    public static WebApplicationService createServiceFrom(
        final HttpServletRequest request) {
        final String service = request.getParameter(CONST_PARAM_SERVICE);

        if (!StringUtils.hasText(service)) {
            return null;
        }

        final String id = cleanupUrl(service);
        final String artifactId = request.getParameter(CONST_PARAM_TICKET);

        return new SamlService(id, service, artifactId);
    }

    public String getRedirectUrl(final String ticketId) {
        final String service = getOriginalUrl();
        final StringBuilder buffer = new StringBuilder(ticketId.length()
            + ticketId.length() + CONST_PARAM_TICKET.length()
            + CONST_PARAM_SERVICE.length() + 4 + service.length());

        buffer.append(service);
        buffer.append(service.contains("?") ? "&" : "?");
        buffer.append(CONST_PARAM_TICKET);
        buffer.append("=");
        try {
            buffer.append(URLEncoder.encode(ticketId, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            buffer.append(ticketId);
        }
        buffer.append("&");
        buffer.append(CONST_PARAM_SERVICE);
        buffer.append("=");

        try {
            buffer.append(URLEncoder.encode(service, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            buffer.append(service);
        }

        return buffer.toString();
    }
}
