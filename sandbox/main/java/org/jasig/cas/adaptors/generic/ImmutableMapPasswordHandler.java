/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.generic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * AuthenticationHandler implementation that authenticates usernames and 
 * passwords against a Map from username to password.  Instances of this class
 * must have such a Map injected at construction.
 * 
 * As we disallow modification of the Map by making a defensive local copy at
 * construction, this implementation can guarantee threadsafety and does not
 * synchronize on the Map at authentication time.  
 * 
 * By virtue of avoiding that synchronization, this implementation is more 
 * performant in processing authentication requests than is 
 * {@link MapPasswordHandler}.
 * 
 * @version $Revision$ $Date$
 * @since 3.0.1
 * @see MapPasswordHandler
 */
public final class ImmutableMapPasswordHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    /** 
     * Map from String username to String password. 
     * We populate this instance variable at construction via defensive copy
     * of our constructor argument.  As such, no other code will be able to
     * edit this Map - we can guarantee it will not chance once we populate it.
     */
    private final Map usernamesToPasswords;
    
    /**
     * Instantiate an ImmutableMapPasswordHandler, seeding it with a Map
     * from String username to String password which it will use as its backing
     * credentials store.  This constructor makes a defensive copy of the Map
     * such that the backing credentials for an ImmutableMapPasswordHandler cannot
     * be modified after the handler is instantiated.
     * @param usernamesToPasswordsArg Map from String username to String password
     */
    public ImmutableMapPasswordHandler(Map usernamesToPasswordsArg) {
    	Map tempUsernamesToPasswords = new HashMap();
    	// defensive copy
    	tempUsernamesToPasswords.putAll(usernamesToPasswordsArg);
    	
    	// ensure that we cannot modify our Map after construction
    	this.usernamesToPasswords = Collections.unmodifiableMap(tempUsernamesToPasswords);
    }

    protected boolean authenticateUsernamePasswordInternal(
			final UsernamePasswordCredentials credentials) {
    	
    	// we need not synchronize on our Map because no other code can edit the
    	// Map.

		// if we know of no valid password for the username, fail the
		// authentication.
		if (!this.usernamesToPasswords.containsKey(credentials.getUsername())) {
			return false;
		}

		final String cachedPassword = 
			(String) this.usernamesToPasswords.get(credentials.getUsername());
		
		if (cachedPassword == null) {
			// our internal Map of usernames to passwords shouldn't contain
			// a null password, but if it did, we fail the authentication in
			// a controlled way rather than incur a NullPointerException by
			// trying to ask this null String if it equals the presented password.
			return false;
		}

		// authentication succeeds if the presented password equals the
		// password in our Map.
		return (cachedPassword.equals(credentials.getPassword()));
	}

    public void afterPropertiesSet() throws Exception {
        // we must implement this method because 
        // AbstractUsernamePasswordAuthenticationHanlder declares the 
        // InitializingBean interface, but we have nothing to do at afterPropertiesSet()
        // as all of our state is injected at construction.
    }
}
