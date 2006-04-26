/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean implementation to create an HttpClient with specific properties.
 * This specific implementation adds the StringSSLProtocolSocketFactory to the
 * HttpClient.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * 
 */
public final class HttpClient3FactoryBean implements FactoryBean,
		InitializingBean {

	/** Instance of HttpClient. */
	private HttpClient httpClient;

	/** Instance of HttpClientParams to populate the HttpClient with. */
	private HttpClientParams httpClientParams = new HttpClientParams();

	public void afterPropertiesSet() throws Exception {
		this.httpClient = new HttpClient(this.httpClientParams);
		Protocol myhttps = new Protocol("https",
				(ProtocolSocketFactory) new StrictSSLProtocolSocketFactory(),
				443);
		Protocol.registerProtocol("https", myhttps);
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

	public void setConnectionManagerClass(final Class clazz) {
		this.httpClientParams.setConnectionManagerClass(clazz);
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
	}

	public void setVersion(final HttpVersion version) {
		this.httpClientParams.setVersion(version);
	}
}
