/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import junit.framework.TestCase;


public class HttpClient3FactoryBeanTests extends TestCase {

    private HttpClient3FactoryBean httpClient3FactoryBean;
    
    public void testSettersAndGetters() throws Exception {
        final boolean CONST_TRUE = true;
        final int CONST_TIMEOUT = 100;
        final String CONST_ENCODING = "UTF-8";
        
        this.httpClient3FactoryBean = new HttpClient3FactoryBean();
        
        
        this.httpClient3FactoryBean.setAuthenticationPreemptive(CONST_TRUE);
        this.httpClient3FactoryBean.setConnectionManagerClass(MultiThreadedHttpConnectionManager.class);
        this.httpClient3FactoryBean.setConnectionManagerTimeout(CONST_TIMEOUT);
        this.httpClient3FactoryBean.setContentCharset(CONST_ENCODING);
        this.httpClient3FactoryBean.setCookiePolicy("policy");
        this.httpClient3FactoryBean.setCredentialCharset(CONST_ENCODING);
        this.httpClient3FactoryBean.setHttpElementCharset(CONST_ENCODING);
        this.httpClient3FactoryBean.setSoTimeout(CONST_TIMEOUT);
        this.httpClient3FactoryBean.setStrict(CONST_TRUE);
        this.httpClient3FactoryBean.setStrict(false);
        this.httpClient3FactoryBean.setVersion(HttpVersion.HTTP_1_1);
        
        this.httpClient3FactoryBean.afterPropertiesSet();
        final HttpClient client = (HttpClient) this.httpClient3FactoryBean.getObject();
        
        assertTrue(client.getParams().isAuthenticationPreemptive());
        assertEquals(MultiThreadedHttpConnectionManager.class, client.getHttpConnectionManager().getClass());
        assertEquals(CONST_TIMEOUT, client.getParams().getConnectionManagerTimeout());
        assertEquals(CONST_ENCODING, client.getParams().getContentCharset());
        assertEquals("policy", client.getParams().getCookiePolicy());
        assertEquals(CONST_ENCODING, client.getParams().getCredentialCharset());
        assertEquals(CONST_ENCODING, client.getParams().getHttpElementCharset());
        assertEquals(CONST_TIMEOUT, client.getParams().getSoTimeout());
        assertEquals(HttpVersion.HTTP_1_1, client.getParams().getVersion());
        assertEquals(HttpClient.class, this.httpClient3FactoryBean.getObjectType());
        assertTrue(this.httpClient3FactoryBean.isSingleton());
    }
}
