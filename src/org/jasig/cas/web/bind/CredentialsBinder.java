/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web.bind;

import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.authentication.principal.Credentials;


/**
 * Interface for a class that can bind items stored in the request to a particular
 * credentials implementation.  This allows for binding beyond the basic
 * JavaBean/Request parameter binding that is handled by Spring automatically.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface CredentialsBinder {
	void bind(HttpServletRequest request, Credentials credentials);
}
