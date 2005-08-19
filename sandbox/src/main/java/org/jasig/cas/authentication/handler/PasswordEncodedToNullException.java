/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.handler.AuthenticationException;

/**
 * Exception to throw when using a PasswordEncoder and that PasswordEncoder
 * returns <code>null</code> as the encoded version of a password and you
 * do not know how to handle such a <code>null</code>.
 * @version $Revision$ $Date$
 */
public class PasswordEncodedToNullException 
    extends AuthenticationException {

    /** The default code for this exception used for message resolving. */
    private static final String CODE = "error.passwordencoder.returnedNull";
    
    /**
     * The PasswordEncoder which returned null, or null if not specified.
     */
    private final PasswordEncoder nullReturningEncoder;
    
    /**
     * Instantiate a PasswordEncodedToNullException accepting the default
     * code.
     */
    public PasswordEncodedToNullException() {
        super(CODE);
        this.nullReturningEncoder = null;
    }
    
    /**
     * Instantiate a PasswordEncodedToNullException specifying a particular code.
     * @param code String keying to the error message
     */
    public PasswordEncodedToNullException(String code) {
        super(code);
        this.nullReturningEncoder = null;
    }
    
    /**
     * Instantiate a PasswordEncodedToNullException accepting the default 
     * error message code and specifying the PasswordEncoder which returned null.
     * @param brokenEncoder encoder which returned null
     */
    public PasswordEncodedToNullException(PasswordEncoder brokenEncoder) {
        super(CODE);
        this.nullReturningEncoder = brokenEncoder;
    }
    
    /**
     * Instantiate a PasswordEncodedToNullException specifying both an error
     * message code and the PasswordEncoder which returned the null value.
     * @param code String keying to the error message
     * @param brokenEncoder encoder which returned null
     */
    public PasswordEncodedToNullException(String code, PasswordEncoder brokenEncoder) {
        super(code);
        this.nullReturningEncoder = brokenEncoder;
    }
    
    /**
     * Get the PasswordEncoder which returned null, or null if not specified.
     * @return the PasswordEncoder which returned null.
     */
    public PasswordEncoder getNullReturningEncoder() {
        return this.nullReturningEncoder;
    }

}

