/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.bind;

import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Interface for a class that can bind items stored in the request to a
 * particular credentials implementation. This allows for binding beyond the
 * basic JavaBean/Request parameter binding that is handled by Spring
 * automatically. Implementations are free to pass part or all of the
 * HttpServletRequest to the Credentials.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 *
 * @deprecated Future versions of CAS will provide a mechanism to gain access to standard items from the Request object.
 */
@Deprecated
public interface CredentialsBinder {

    /**
     * Method to allow manually binding attributes from the request object to
     * properties of the credentials. Useful when there is no mapping of
     * attribute to property for the usual Spring binding to handle.
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
    boolean supports(Class<?> clazz);
}
