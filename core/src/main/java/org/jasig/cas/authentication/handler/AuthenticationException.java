/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

/**
 * The most generic type of authentication exception when one cannot determine
 * why the authentication actually failed. Top of the tree of all other
 * AuthenticationExceptions
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AuthenticationException extends Exception {

    /** Serializable ID. */
    private static final long serialVersionUID = 3906648604830611762L;
    
    /** The code to return for resolving to a message description. */
    private String code = "generic_error";

    public final String getCode() {
        return this.code;
    }
    
    protected final void setCode(final String code) {
        this.code = code;
    }
    
    public final String toString() {
        return this.code;
    }
}
