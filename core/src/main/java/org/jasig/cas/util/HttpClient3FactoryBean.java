/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean implementation to create an HttpClient with specific properties.
 * This specific implementation adds the StringSSLProtocolSocketFactory to the
 * HttpClient.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class HttpClient3FactoryBean implements FactoryBean,
    InitializingBean, DisposableBean {

    /** Instance of Commons Logging. */
    private Log log = LogFactory.getLog(this.getClass());

    /** Instance of HttpClient. */
    private HttpClient httpClient;

    /** Instance of HttpConnectionManager */
    private MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

    /** Instance of HttpClientParams to populate the HttpClient with. */
    private HttpClientParams httpClientParams = new HttpClientParams();
    
    /** Boolean to determine whether to use strict host name checking or not. Enabled by default. */
    private boolean useStrictHostNameChecking = true;

    /**
     * Instance of HttpConnectionManagerParams to populate the
     * HttpConnectionManager.
     */
    private HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();

    public void afterPropertiesSet() throws Exception {
        this.httpClient = new HttpClient(this.httpClientParams, this.manager);
        this.manager.setParams(this.httpConnectionManagerParams);

        this.httpConnectionManagerParams.setStaleCheckingEnabled(true);

        if (!this.useStrictHostNameChecking) {
            log.warn("Strict host name checking disabled.  DO NOT DO THIS IN PRODUCTION.");
        }

        final StrictSSLProtocolSocketFactory factory = new StrictSSLProtocolSocketFactory();
        factory.setHostnameVerification(this.useStrictHostNameChecking);
        Protocol myhttps = new Protocol("https",
            (ProtocolSocketFactory) factory, 443);
        Protocol.registerProtocol("https", myhttps);
    }

    public void destroy() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down MultiThreadedHttpConnectionManager...");
        }

        this.manager.shutdown();
    }

    public Object getObject() throws Exception {
        return this.httpClient;
    }

    public Class getObjectType() {
        return HttpClient.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setStrict(final boolean strict) {
        if (strict) {
            this.httpClientParams.makeStrict();
        } else {
            this.httpClientParams.makeLenient();
        }
    }

    public void setAuthenticationPreemptive(final boolean value) {
        this.httpClientParams.setAuthenticationPreemptive(value);
    }

    public void setConnectionManagerTimeout(final long timeout) {
        this.httpClientParams.setConnectionManagerTimeout(timeout);
    }

    public void setContentCharset(final String charset) {
        this.httpClientParams.setContentCharset(charset);
    }

    public void setCookiePolicy(final String policy) {
        this.httpClientParams.setCookiePolicy(policy);
    }

    public void setCredentialCharset(final String charset) {
        this.httpClientParams.setCredentialCharset(charset);
    }

    public void setHttpElementCharset(final String charset) {
        this.httpClientParams.setHttpElementCharset(charset);
    }

    public void setSoTimeout(final int timeout) {
        this.httpClientParams.setSoTimeout(timeout);
        this.httpConnectionManagerParams.setSoTimeout(timeout);
    }

    public void setVersion(final HttpVersion version) {
        this.httpClientParams.setVersion(version);
    }

    public void setDefaultMaxConnectionsPerHost(final int maxHostConnections) {
        this.httpConnectionManagerParams
            .setDefaultMaxConnectionsPerHost(maxHostConnections);
    }

    public void setMaxTotalConnections(final int maxTotalConnections) {
        this.httpConnectionManagerParams
            .setMaxTotalConnections(maxTotalConnections);
    }

    public void setConnectionTimeout(final int connectionTimeout) {
        this.httpConnectionManagerParams
            .setConnectionTimeout(connectionTimeout);
    }

    public void setUseStrictHostNameChecking(final boolean useStrictHostNameChecking) {
        this.useStrictHostNameChecking = useStrictHostNameChecking;
    } 
}
