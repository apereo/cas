/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.proxy.support;

import java.io.IOException;

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

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class Cas20ProxyHandler implements ProxyHandler, InitializingBean {
    protected final Log log = LogFactory.getLog(getClass());
    private static final String PGTIOU_PREFIX = "PGTIOU";

    private UniqueTicketIdGenerator uniqueTicketIdGenerator;

    private HttpClient httpClient;

    /**
     * @see org.jasig.cas.ticket.proxy.ProxyHandler#handle(org.jasig.cas.authentication.principal.Credentials)
     */
    public String handle(Credentials credentials, String proxyGrantingTicketId) {
        final HttpBasedServiceCredentials serviceCredentials = (HttpBasedServiceCredentials)credentials;
        final String proxyIou = uniqueTicketIdGenerator.getNewTicketId(PGTIOU_PREFIX);
        final StringBuffer stringBuffer = new StringBuffer();
        final String callbackUrl;
        final GetMethod getMethod;

        stringBuffer.append(serviceCredentials.getCallbackUrl().toExternalForm());

        if (serviceCredentials.getCallbackUrl().getQuery() != null)
            stringBuffer.append("&");
        else
            stringBuffer.append("?");

        stringBuffer.append("pgtIou=");
        stringBuffer.append(proxyIou);
        stringBuffer.append("&pgtId=");
        stringBuffer.append(proxyGrantingTicketId);

        getMethod = new GetMethod(stringBuffer.toString());

        try {
            httpClient.executeMethod(getMethod);
            final String response = getMethod.getResponseBodyAsString();
            getMethod.releaseConnection();

            if (response != null) {
                return proxyIou;
            }
        }
        catch (IOException ioe) {
        }
        finally {
            getMethod.releaseConnection();
        }

        return null;
    }
    
    /**
     * @param httpClient The httpClient to set.
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    /**
     * @param uniqueTicketIdGenerator The uniqueTicketIdGenerator to set.
     */
    public void setUniqueTicketIdGenerator(UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.httpClient == null) {
            this.httpClient = new HttpClient();
            log.info("No HttpClient specified for " + this.getClass().getName() + ".  Using default HttpClient settings.");
            
        }
        
        if (this.uniqueTicketIdGenerator == null) {
            this.uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();
            log.info("No UniqueTicketIdGenerator specified for " + this.getClass().getName() + ".  Using " + this.uniqueTicketIdGenerator.getClass().getName());
        }
    }
}
