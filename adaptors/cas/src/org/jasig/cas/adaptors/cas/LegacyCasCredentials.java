/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.cas;

import javax.servlet.ServletRequest;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Credentials class that maps to the parameters required by the Legacy CAS password handler.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class LegacyCasCredentials extends UsernamePasswordCredentials {

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
    public void setServletRequest(ServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}
