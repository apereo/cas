/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

/**
 * Various constants used by CAS web tier components.
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public abstract class WebConstants {
	public static final String CONST_COOKIE_TGC_ID = "CASTGC"; 
	public static final String CONST_COOKIE_PRIVACY = "CASPRIVACY";
	
	public static final String CONST_COOKIE_DEFAULT_EMPTY_VALUE = "false";
	public static final String CONST_COOKIE_DEFAULT_FILLED_VALUE = "true";
	
	public static final String CONST_MODEL_TICKET = "ticket";
	public static final String CONST_MODEL_CODE = "code";
	public static final String CONST_MODEL_DESC = "description";
	public static final String CONST_MODEL_PRINCIPAL = "principal";
	public static final String CONST_MODEL_PGTIOU = "pgtiou";
	public static final String CONST_MODEL_PROXIES = "proxies";
	public static final String CONST_MODEL_LOGIN_TICKET = "loginTicket";
	public static final String CONST_MODEL_AUTHENTICATION_REQUEST = "authenticationRequest";
	public static final String CONST_MODEL_SERVICE = "service";
	public static final String CONST_MODEL_FIRST = "first";
	public static final String CONST_MODEL_CAS_ATTRIBUTES = "casAttributes";
}
