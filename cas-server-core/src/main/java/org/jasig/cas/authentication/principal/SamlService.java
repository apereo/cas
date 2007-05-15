/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.springframework.util.StringUtils;

/**
 * Class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.6 $ $Date: 2007/02/27 19:31:58 $
 * @since 3.1
 */
public final class SamlService extends AbstractWebApplicationService {

    private static final Log LOG = LogFactory.getLog(SamlService.class);

    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    /** Constant representing service. */
    private static final String CONST_PARAM_SERVICE = "TARGET";

    /** Constant representing artifact. */
    private static final String CONST_PARAM_TICKET = "SAMLart";

    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    private boolean loggedOutAlready = false;

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

    public synchronized boolean logOutOfService(final String sessionIdentifier) {
        if (this.loggedOutAlready) {
            return true;
        }

        final DateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(UTC_TIME_ZONE);
        final String date = dateFormat.format(new Date());

        LOG.debug("Sending logout request for: " + getId());

        final String logoutRequest = "<samlp:LogoutRequest ID=\""
            + GENERATOR.getNewTicketId("LR")
            + "\" Version=\"2.0\" IssueInstant=\"" + date
            + "\"><saml:NameID>NotUsed</saml:NameID><samlp:SessionIndex>"
            + sessionIdentifier + "</samlp:SessionIndex></samlp:LogoutRequest>";

        HttpURLConnection connection = null;
        try {
            final URL logoutUrl = new URL(getOriginalUrl());
            final String output = "logoutRequest=" + logoutRequest;

            connection = (HttpURLConnection) logoutUrl.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Length", ""
                + Integer.toString(output.getBytes().length));
            connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
            final DataOutputStream printout = new DataOutputStream(connection
                .getOutputStream());
            printout.writeBytes(output);
            printout.flush();
            printout.close();

            final BufferedReader in = new BufferedReader(new InputStreamReader(connection
                .getInputStream()));

            while (in.readLine() != null) {
                // nothing to do
            }
            
            return true;
        } catch (final MalformedURLException e) {
            return false;
        } catch (final IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            this.loggedOutAlready = true;
        }
    }
}
