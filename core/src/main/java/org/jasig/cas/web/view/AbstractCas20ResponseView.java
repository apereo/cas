/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import org.apache.xmlbeans.XmlOptions;

/**
 * <p>Base methods for all CAS 2.0 validation responses.  These responses
 * are implemented using XMLBeans.</p>
 * 
 * @author Drew Mazurek
 * @version $Revision$ $Date$
 * @since 3.0.1
 */
public abstract class AbstractCas20ResponseView implements View {

	private XmlOptions xmlOptions = 
		new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4);
    private String httpContentType;
    private String httpCharset;
	protected final String DEFAULT_HTTP_CONTENT_TYPE = "text/html";
	protected final String DEFAULT_HTTP_CHARSET = "ISO-8859-1";
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public abstract void render(final Map model, 
			final HttpServletRequest request,
			final HttpServletResponse response) throws Exception;
	
	/**
	 * Sets an XmlOptions object to override the default
	 * @param xmlOptions
	 */
	protected final void setXmlOptions(final XmlOptions xmlOptions) {
		if(xmlOptions != null) {
			this.xmlOptions = xmlOptions;
		}
	}

	/**
	 * Gets the XmlOptions object
	 * @return xmlOptions
	 */
	protected final XmlOptions getXmlOptions() {
		return this.xmlOptions;
	}
	
	/**
	 * Sets the Content-Type of the HTTP response (e.g. text/xml).
	 * Do not specify character encoding here!
	 * @param contentType
	 */
	public final void setHttpContentType(final String contentType) {
		this.httpContentType = contentType;
	}
	
	/**
	 * Gets the Content-Type of the HTTP response.  If none was set,
	 * returns DEFAULT_HTTP_CONTENT_TYPE.
	 * @return contentType
	 */
	protected final String getHttpContentType() {
		if(this.httpContentType != null) {
			return this.httpContentType;
		} else {
			return this.DEFAULT_HTTP_CONTENT_TYPE;
		}
	}

	/**
	 * Sets the character set of the HTTP response (e.g. ISO-8859-1).
	 * @param httpCharset
	 */
	public final void setHttpCharset(final String httpCharset) {
		this.httpCharset = httpCharset;
	}
	
	/**
	 * Gets the character set of the HTTP response.  If none was set,
	 * returns DEFAULT_HTTP_CHARSET.
	 * @return httpCharset
	 */
	public final String getHttpCharset() {
		if(this.httpCharset != null) {
			return this.httpCharset;
		} else {
			return this.DEFAULT_HTTP_CHARSET;
		}
	}


}
