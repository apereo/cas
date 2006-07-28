/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.proxy.support;

import java.net.HttpURLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Proxy Handler to handle the default callback functionality of CAS 2.0.
 * <p>
 * The default behavior as defined in the CAS 2 Specification is to callback the
 * URL provided and give it a pgtIou and a pgtId.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas20ProxyHandler implements ProxyHandler, InitializingBean {

    /** The Commons Logging instance. */
    private final Log log = LogFactory.getLog(getClass());

    /** The PGTIOU ticket prefix. */
    private static final String PGTIOU_PREFIX = "PGTIOU";
    
    /** The default status codes we accept. */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[] {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED,
        HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED};

    /** List of HTTP status codes considered valid by this AuthenticationHandler. */
    private int[] acceptableCodes;

    /** Generate unique ids. */
    private UniqueTicketIdGenerator uniqueTicketIdGenerator;
    
    /** Instance of Apache Commons HttpClient */
    private HttpClient httpClient;

    public String handle(final Credentials credentials,
        final String proxyGrantingTicketId) {
        final HttpBasedServiceCredentials serviceCredentials = (HttpBasedServiceCredentials) credentials;
        final String proxyIou = this.uniqueTicketIdGenerator
            .getNewTicketId(PGTIOU_PREFIX);
        final StringBuffer stringBuffer = new StringBuffer();

        synchronized (stringBuffer) {
            stringBuffer.append(serviceCredentials.getCallbackUrl()
                .toExternalForm());
    
            if (serviceCredentials.getCallbackUrl().getQuery() != null) {
                stringBuffer.append("&");
            } else {
                stringBuffer.append("?");
            }
    
            stringBuffer.append("pgtIou=");
            stringBuffer.append(proxyIou);
            stringBuffer.append("&pgtId=");
            stringBuffer.append(proxyGrantingTicketId);
        }

        final GetMethod getMethod = new GetMethod(stringBuffer.toString());
        try {
            this.httpClient.executeMethod(getMethod);
            final int responseCode = getMethod.getStatusCode();
            for (int i = 0; i < this.acceptableCodes.length; i++) {
                if (responseCode == this.acceptableCodes[i]) {
                    if (log.isDebugEnabled()) {
                        log.debug("Sent ProxyIou of " + proxyIou + " for service: "
                            + serviceCredentials.getCallbackUrl());
                    }

                    return proxyIou;
                }
             }
        } catch (final Exception e) {
            log.error(e,e);
            // do nothing
        } finally {
            getMethod.releaseConnection();
        }

        if (log.isDebugEnabled()) {
            log.debug("Failed to send ProxyIou of " + proxyIou
                + " for service: " + serviceCredentials.getCallbackUrl());
        }
        return null;
    }

    /**
     * @param uniqueTicketIdGenerator The uniqueTicketIdGenerator to set.
     */
    public void setUniqueTicketIdGenerator(
        final UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
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
    
    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.httpClient, "httpClient cannot be null.");

        if (this.uniqueTicketIdGenerator == null) {
            this.uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();
            log.info("No UniqueTicketIdGenerator specified for "
                + this.getClass().getName() + ".  Using "
                + this.uniqueTicketIdGenerator.getClass().getName());
        }
        
           if (this.acceptableCodes == null) {
            this.acceptableCodes = DEFAULT_ACCEPTABLE_CODES;
        }
    }
}
