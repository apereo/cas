/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.cas;

import javax.servlet.ServletRequest;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Class to wrap the HttpServletRequest object opaquely in the Credentials. The
 * LegacyPasswordHandlerAdapter extracts this object so it can be presented to a
 * CAS 2 PasswordHandler.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LegacyCasCredentials extends UsernamePasswordCredentials {

    private static final long serialVersionUID = 3256442508274775608L;

    private ServletRequest servletRequest;

    /**
     * @return Returns the servletRequest.
     */
    public ServletRequest getServletRequest() {
        return this.servletRequest;
    }

    /**
     * @param servletRequest The servletRequest to set.
     */
    public void setServletRequest(final ServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}