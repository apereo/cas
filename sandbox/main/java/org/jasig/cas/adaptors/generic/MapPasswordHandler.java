/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.generic;

import java.util.Map;

import org.jasig.cas.authentication.handler.AbstractPasswordHandler;

import org.springframework.beans.factory.InitializingBean;

/**
 * AuthenticationHandler implementation that authenticates usernames and 
 * passwords against a Map from username to password.  Instances of this class
 * must have such a Map injected via {@link #setUsernamesToPasswords(Map)} 
 * before authenticateUsernamePasswordInternal() may be invoked.
 * 
 * This AuthenticationHandler is useful as a supplemental handler (suppose you
 * have most users stored in a Kerberos realm but additionally have a few 
 * usernames you wish to be able to CAS authenticate though they are not reflected
 * in other backing credential stores).
 * 
 * This AuthenticationHandler is also useful as a bridge between the CAS 
 * AuthenticationHandler API and any other module capable of populating a Map.
 * That is, this class allows you to connect a Map produced and ongoingly managed
 * by some other piece of code into the Authentication module.
 * 
 * This implementation synchronizes on its Map
 * from username to password (provided via the 
 * {@link #setUsernamesToPasswords(Map)} method) in its implementation of 
 * {@link #authenticateInternal(String, String)}.  
 * Any other code modifying the Map should synchonize on the Map before 
 * modifying it to ensure that this Handler has a consistent view on the Map for 
 * any given authentication attempt and does not run afoul of concurrent 
 * modification problems.
 * 
 * The backing Map of usernames to passwords can be re-injected (overridden)
 * at any time by calling the setter method.  The new backing map will take effect
 * for subsequent authenticate() invocations.
 * 
 * If the backing Map of credentials will not change after instantatiation of this 
 * AuthenticationHandler, then you may be able to avoid the synchronization performed
 * by this implementation and pick up better performance by using
 * {@link ImmutableMapPasswordHandler}.
 * 
 * @version $Revision$ $Date$
 * @since 3.0.1
 * @see ImmutableMapPasswordHandler
 */
public final class MapPasswordHandler 
    extends AbstractPasswordHandler 
    implements InitializingBean {

    /** Map from String username to String password. */
    private Map usernamesToPasswords;


    public void afterPropertiesSet() throws Exception {
        if (this.usernamesToPasswords == null) {
            throw new IllegalStateException("usernamesToPasswords must be set on "
                + this.getClass().getName());
        }
    }

    /**
     * Set the Map from String usernames to String passwords defining our backing
     * credentials.  This AuthenticationHandler implementation will synchronize on 
     * this Map when accessing it, so external code can sychronize on the Map 
     * before changing it to apply changes to our backing credentials at runtime.
     * 
     * This method may be invoked to inject a new backing credentials Map at any 
     * time.  The new backing credentials will take effect for subsequent invocations
     * of authenticate().
     * 
     * @param usernamesToPasswords Map from String usernames to String passwords.
     * @throws IllegalArgumentException if argument is null
     */
    public void setUsernamesToPasswords(final Map usernamesToPasswords) {
    	if (usernamesToPasswords == null) {
    		throw new IllegalArgumentException("Cannot set the property 'usernamesToPasswords' of a MapPasswordHandler to null.");
    	}
    	
        this.usernamesToPasswords = usernamesToPasswords;
    }

    protected boolean authenticateInternal(String username, String password) {

        // use a local reference to the Map so that we will use the same Map,
        // the Map upon which we synchronize, 
        // throughout this method call, regardless of a new Map being injected
        // during this method run
        Map localUsernamesToPasswords = this.usernamesToPasswords;
        
        // synchronize on our Map of usernames to passwords to preclude
        // concurrent modification by any process that periodically or ongoingly
        // updates the map (and properly synchronizes before performing those
        // changes)
        synchronized (localUsernamesToPasswords) {

            // if we know of no valid password for the username, fail the
            // authentication.
            if (! localUsernamesToPasswords.containsKey(username)) {
                if (log.isTraceEnabled()) {
                    log.trace("Failed to authenticate [" + username + "] because username is not recognized.");
                }
                return false;
            }

            final String cachedPassword = (String) localUsernamesToPasswords.get(username);
            
            // the password in our credentials store shouldn't be null, but if it is, 
            // fail to authenticate rather than throwing NullPointerException when we
            // try to do the String equality comparison.
            if (cachedPassword == null) {
                log.warn("Failed to authenticate [" + username + "] because stored password for this user was null.");
                return false;
            }
            

            // authentication succeeds if the presented password equals the
            // password in our Map.
            final boolean passwordMatched =  cachedPassword.equals(password);
            
            if (log.isTraceEnabled()) {
                if (passwordMatched) {
                    log.trace("Authenticating [" + username + "] because presented password matched stored password.");
                } else {
                    log.trace("Failing to authenticate [" + username + "] because presented password did not match stored password");
                }
            }
            
            return passwordMatched;

        }
    }
}
