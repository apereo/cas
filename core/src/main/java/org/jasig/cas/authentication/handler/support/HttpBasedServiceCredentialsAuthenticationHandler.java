/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.net.HttpURLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.springframework.beans.factory.InitializingBean;

/**
 * Class to validate the credentials presented by communicating with the web
 * server and checking the certificate that is returned against the hostname,
 * etc.
 * <p>
 * This class is concerned with ensuring that the protocol is HTTPS and that a
 * response is returned. The SSL handshake that occurs automatically by opening
 * a connection does the heavy process of authenticating.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsAuthenticationHandler implements
    AuthenticationHandler, InitializingBean {

    /** The string representing the HTTPS protocol. */
    private static final String PROTOCOL_HTTPS = "https";

    /** The default status codes we accept. */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[] {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED,
        HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED};

    /** List of HTTP status codes considered valid by this AuthenticationHandler. */
    private int[] acceptableCodes;

    /** Boolean variable denoting whether secure connection is required or not. */
    private boolean requireSecure = true;

    /** Log instance. */
    private final Log log = LogFactory.getLog(getClass());

    /** Instance of Apache Commons HttpClient */
    private HttpClient httpClient;

    public boolean authenticate(final Credentials credentials) {
        final HttpBasedServiceCredentials serviceCredentials = (HttpBasedServiceCredentials) credentials;
        if (this.requireSecure
            && !serviceCredentials.getCallbackUrl().getProtocol().equals(
                PROTOCOL_HTTPS)) {
            if (log.isDebugEnabled()) {
                log.debug("Authentication failed because url was not secure.");
            }
            return false;
        }
        log
            .debug("Attempting to resolve credentials for "
                + serviceCredentials);

        final GetMethod getMethod = new GetMethod(serviceCredentials
            .getCallbackUrl().toExternalForm());
        int responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
        try {
            this.httpClient.executeMethod(getMethod);
            responseCode = getMethod.getStatusCode();
            for (int i = 0; i < this.acceptableCodes.length; i++) {
                if (responseCode == this.acceptableCodes[i]) {
                    return true;
                }
            }
        } catch (final Exception e) {
            log.error(e, e);
            // do nothing
        } finally {
            getMethod.releaseConnection();
        }

        if (log.isDebugEnabled()) {
            log
                .debug("Authentication failed because returned status code was ["
                    + responseCode + "]");
        }

        return false;
    }

    /**
     * @return true if the credentials provided are not null and the credentials
     * are a subclass of (or equal to) HttpBasedServiceCredentials.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null
            && HttpBasedServiceCredentials.class.isAssignableFrom(credentials
                .getClass());
    }

    /**
     * Set the acceptable HTTP status codes that we will use to determine if the
     * response from the URL was correct.
     * 
     * @param acceptableCodes an array of status code integers.
     */
    public void setAcceptableCodes(final int[] acceptableCodes) {
        this.acceptableCodes = acceptableCodes;
    }

    /** Sets the HttpClient which will do all of the connection stuff. */
    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Set whether a secure url is required or not.
     * 
     * @param requireSecure true if its required, false if not. Default is true.
     */
    public void setRequireSecure(final boolean requireSecure) {
        this.requireSecure = requireSecure;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.acceptableCodes == null) {
            this.acceptableCodes = DEFAULT_ACCEPTABLE_CODES;
        }

        if (this.httpClient == null) {
            this.httpClient = new HttpClient();
            Protocol myhttps = new Protocol(
                    "https",
                    (ProtocolSocketFactory) new StrictSSLProtocolSocketFactory(),
                    443);
            Protocol.registerProtocol("https", myhttps);
        }
    }
}
