/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class HttpBasedServiceCredentialsAuthenticationHandler implements AuthenticationHandler, InitializingBean {
    protected final Log log = LogFactory.getLog(getClass());
    
    private static final String PROTOCOL_HTTPS = "https";

    private HttpClient httpClient;
    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.principal.Credentials)
     */
    public boolean authenticate(Credentials credentials) throws AuthenticationException {
        final HttpBasedServiceCredentials serviceCredentials = (HttpBasedServiceCredentials) credentials;
        final GetMethod getMethod = new GetMethod(serviceCredentials.getCallbackUrl().toExternalForm());
        
        if (!serviceCredentials.getCallbackUrl().getProtocol().equals(PROTOCOL_HTTPS)) {
            return false;
        }
        
        try {
            this.httpClient.executeMethod(getMethod);
            getMethod.releaseConnection();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#supports(org.jasig.cas.authentication.principal.Credentials)
     */
    public boolean supports(Credentials credentials) {
        return HttpBasedServiceCredentials.class.isAssignableFrom(credentials.getClass());
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.httpClient == null) {
            this.httpClient = new HttpClient();
            Protocol.registerProtocol(PROTOCOL_HTTPS, new Protocol(PROTOCOL_HTTPS, new StrictSSLProtocolSocketFactory(), 443)); 
            log.info("No HttpClient specified for " + this.getClass().getName() + ".  Default settings used for HttpClient.");
        }
    }
}
