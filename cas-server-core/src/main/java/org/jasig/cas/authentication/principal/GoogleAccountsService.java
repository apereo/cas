/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.webflow.util.Base64;
import org.w3c.dom.Document;

/**
 * Implementation of a Service that supports Google Accounts (eventually a more
 * generic SAML2 support will come).
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public class GoogleAccountsService extends AbstractWebApplicationService {

    private static final Log LOG = LogFactory
        .getLog(GoogleAccountsService.class);

    private static final String CONST_PARAM_SERVICE = "SAMLRequest";

    private static final String CONST_RELAY_STATE = "RelayState";

    private final String relayState;

    protected GoogleAccountsService(final String id, final String relayState) {
        this(id, id, null, relayState);
    }

    protected GoogleAccountsService(final String id, final String originalUrl,
        final String artifactId, final String relayState) {
        super(id, originalUrl, artifactId);
        this.relayState = relayState;
    }

    public static WebApplicationService createServiceFrom(
        final HttpServletRequest request) {
        // the SAML request
        final String relayState = request.getParameter(CONST_RELAY_STATE);
        final String xmlRequest = decodeAuthnRequestXML(request
            .getParameter(CONST_PARAM_SERVICE));

        if (!StringUtils.hasText(xmlRequest)) {
            return null;
        }

        final Document document = constructDocumentFromXmlString(xmlRequest);

        if (document == null) {
            return null;
        }

        final String assertionConsumerServiceUrl = document.getAttributes()
            .getNamedItem("AssertionConsumerServiceURL").getNodeValue();

        return new GoogleAccountsService(assertionConsumerServiceUrl,
            relayState);
    }

    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<String, String>();

        // TODO get samlresponse
        parameters.put("SAMLResponse", "");
        parameters.put("RelayState", this.relayState);

        return Response.getPostResponse(getOriginalUrl(), parameters);
    }

    /**
     * Service does not support Single Log Out
     * 
     * @see org.jasig.cas.authentication.principal.WebApplicationService#logOutOfService(java.lang.String)
     */
    public boolean logOutOfService(final String sessionIdentifier) {
        return false;
    }

    private static String decodeAuthnRequestXML(
        final String encodedRequestXmlString) {
        if (encodedRequestXmlString == null) {
            return null;
        }

        final byte[] decodedBytes = base64Decode(encodedRequestXmlString);

        if (decodedBytes == null) {
            return null;
        }

        final String inflated = inflate(decodedBytes);

        if (inflated != null) {
            return inflated;
        }

        return zlibDeflate(decodedBytes);
    }

    private static String zlibDeflate(final byte[] bytes) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final InflaterInputStream iis = new InflaterInputStream(bais);
        final byte[] buf = new byte[1024];

        try {
            int count = iis.read(buf);
            while (count != -1) {
                baos.write(buf, 0, count);
                count = iis.read(buf);
            }
            return new String(baos.toByteArray());
        } catch (final Exception e) {
            return null;
        } finally {
            try {
                iis.close();
            } catch (final Exception e) {
                // nothing to do
            }
        }
    }

    private static byte[] base64Decode(final String xml) {
        try {
            final Base64 base64Decoder = new Base64();
            final byte[] xmlBytes = xml.getBytes("UTF-8");
            return base64Decoder.decode(xmlBytes);
        } catch (final Exception e) {
            return null;
        }
    }

    private static String inflate(final byte[] bytes) {
        final Inflater inflater = new Inflater(true);
        final byte[] xmlMessageBytes = new byte[5000];
        inflater.setInput(bytes);

        try {
            final int resultLength = inflater.inflate(xmlMessageBytes);

            if (!inflater.finished()) {
                throw new RuntimeException("buffer not large enough.");
            }

            inflater.end();
            return new String(xmlMessageBytes, 0, resultLength, "UTF-8");
        } catch (final DataFormatException e) {
            return null;
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot find encoding: UTF-8", e);
        }
    }

    private static Document constructDocumentFromXmlString(final String xml) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(xml);
        } catch (final Exception e) {
            LOG.error(e, e);
            return null;
        }
    }
}
