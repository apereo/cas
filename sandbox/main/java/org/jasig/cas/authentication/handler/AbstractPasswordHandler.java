/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Abstract class that simplifies implementing AuthenticationHandler for username 
 * and password authentication down to implementing a method that takes 
 * username and password Strings.  Applies a configured PasswordEncoder.  
 * 
 * Implementing such username password handling strategies as comparing them
 * against a database table, a text file, an XML source, an in-memory Map,
 * etc. as subclasses of this class offers the advantage of support for
 * PasswordEncoders.  That is, a handler subclassing this which compares the
 * username, password pair against a database table can be configured to check
 * for the password plaintext in the table or an MD5 hash on the password in
 * the table by plugging in different PasswordEncoders.
 * 
 * Unless another PasswordEncoder is injected, the default PasswordEncoder is
 * a no-op PasswordEncoder which passes through the password unmodified.
 * @version $Revision$ $Date$
 */
public abstract class AbstractPasswordHandler 
    implements AuthenticationHandler {
    
    /** 
     * Commons Logging Log instance.
     */
    protected final Log log = LogFactory.getLog(getClass());
    
    /**
     * Transforms the password as presented to a form suitable for comparison
     * to the backing credential store.  By default we apply a no-op transformation.
     * Potential transformations include computation of a secure hash, normalization, etc.
     */
    private PasswordEncoder encoder = new PlainTextPasswordEncoder();

    public boolean authenticate(Credentials credentials) 
        throws AuthenticationException {
        
        if (! supports(credentials)) {
            log.error("Cannot authenticate these unsupported credentials: " + credentials);
            throw new UnsupportedCredentialsException();
        }
        
        // we now know that the credentials are of class UsernamePasswordCredentials
        // and that they bear a non-null username and password.
        
        UsernamePasswordCredentials usernamePassword = 
            (UsernamePasswordCredentials) credentials;
        
        final String username = usernamePassword.getUsername();
        
        final String encodedPassword = 
            this.encoder.encode(usernamePassword.getPassword());
        
        if (encodedPassword == null) {
            throw new PasswordEncodedToNullException(this.encoder);
        }
        
        // we've guaranteed that the arguments to the internal method are non-null
        return authenticateInternal(username, encodedPassword);
        
    }

    /**
     * Return true if the password authenticates the username, return false otherwise.
     * This password will already be transformed as defined by the injected 
     * password encoder, if any.
     * @param username Non-null String, the username as which the request is attempting to authenticate
     * @param password Non-null String potentially encoded password
     * @return true if the password authenticates the username, false otherwise
     */
    abstract protected boolean authenticateInternal(String username, String password);

    public boolean supports(Credentials credentials) {
        // non-final to allow subclasses to apply additional or alternative supports rules
        
        if (! (credentials instanceof UsernamePasswordCredentials)) {
            if (log.isTraceEnabled()) {
                log.trace("This credentials [" + credentials + "] is not a UsernamePasswordCredentials, so we do not support it.");
            }
            return false;
        }
        
        // we only suppoort UsernamePasswordCredentials where both the 
        // username and the password are non-null
        
        UsernamePasswordCredentials usernamePassword = 
            (UsernamePasswordCredentials) credentials;
        
        if (usernamePassword.getUsername() == null) {
            log.trace("The credentials [" + credentials + "] presents a null username so we do not support it.");
            return false;
        }
        
        if (usernamePassword.getPassword() == null) {
            log.trace("The credentials [" + credentials + "] presents a null password so we do not support it.");
            return false;
        }
        
        if (log.isTraceEnabled()) {
            log.trace("The credentials was of type UsernamePasswordCredentials and bears non-null username and password, so we support it.");
        }
        
        return true;
    }

    /**
     * Get the PasswordEncoder we will apply to passwords before presenting them
     * to authenticateInternal().
     * @return Returns the encoder.
     */
    public final PasswordEncoder getEncoder() {
        return this.encoder;
    }
    
    /**
     * Set the PasswordEncoder we will apply to passwords before presenting them
     * to authenticateInternal().
     * @param encoder non-null encoder to be applied to passwords.
     * @throws IllegalArgumentException if argument is null
     */
    public final void setEncoder(PasswordEncoder encoder) {
        if (encoder == null) {
            throw new IllegalArgumentException("Cannot set encoder to null.");
        }
        this.encoder = encoder;
    }
}

