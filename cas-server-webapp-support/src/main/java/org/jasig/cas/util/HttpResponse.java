/*
 * @(#)HttpResponse.java $version 2015. 1. 28.
 *
 * Copyright 2014 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.jasig.cas.util;

import javax.servlet.http.Cookie;
import java.util.List;

/**
 * @author ruaa
 * @since 2015. 1. 28.
 */
public class HttpResponse<T> {
	private int responseCode;
	private T responseBody;

	public List<Cookie> getCookieList() {
		return cookieList;
	}

	public void setCookieList(List<Cookie> cookieList) {
		this.cookieList = cookieList;
	}

	private List<Cookie> cookieList;
	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}
	/**
	 * @param responseCode the responseCode to set
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	/**
	 * @return the responseBody
	 */
	public T getResponseBody() {
		return responseBody;
	}
	/**
	 * @param responseBody the responseBody to set
	 */
	public void setResponseBody(T responseBody) {
		this.responseBody = responseBody;
	}
	
}
