/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.bind;

import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Bind Web layer HttpServletRequest object to a Credentials object.
 * 
 * <p>The javax.servlet classes should be limited to the Web layer. The 
 * CAS layer is not permitted to have Servlet API references. However,
 * to support Legacy PasswordHandlers (from CAS 2) we need 
 * to be able to present the HttpServletRequest as part of the call.</p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface CredentialsBinder {

    /**
     * Attach HttpServletRequest to Credentials object.
     * 
     * @param request The HttpServletRequest from which we wish to bind
     * credentials to
     * @param credentials The credentials we will be doing custom binding to.
     */
    void bind(HttpServletRequest request, Credentials credentials);

    /**
     * Method to determine if a CredentialsBinder supports a specific class or
     * not.
     * 
     * @param clazz The class to determine is supported or not
     * @return true if this class is supported by the CredentialsBinder, false
     * otherwise.
     */
    boolean supports(Class clazz);
}
