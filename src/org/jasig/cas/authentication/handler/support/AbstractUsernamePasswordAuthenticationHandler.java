/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.AuthenticationRequest;
import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.springframework.beans.factory.InitializingBean;


/**
 * Abstract class to override supports so that we don't need to duplicate
 * the check for UsernamePasswordAuthenticationRequest.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public abstract class AbstractUsernamePasswordAuthenticationHandler implements AuthenticationHandler, InitializingBean {
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * 
	 * @see org.jasig.cas.authentication.handler.AuthenticationHandler#supports(org.jasig.cas.authentication.AuthenticationRequest)
	 */
	public boolean supports(final AuthenticationRequest request) {
		return request instanceof UsernamePasswordAuthenticationRequest;
	}

}
