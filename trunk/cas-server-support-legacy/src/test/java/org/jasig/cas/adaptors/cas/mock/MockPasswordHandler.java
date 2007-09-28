/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */

package org.jasig.cas.adaptors.cas.mock;

import javax.servlet.ServletRequest;

import edu.yale.its.tp.cas.auth.PasswordHandler;

/**
 * Mock PasswordHandler implementation in support of testcases.
 * 
 * @version $Revision$ $Date$
 */
public class MockPasswordHandler implements PasswordHandler {

    /**
     * Value this object will return on invocation of its interface method.
     */
    private boolean succeed;

    private ServletRequest request;

    private String username;

    private String password;

    public boolean authenticate(ServletRequest requestArg, String usernameArg,
        String passwordArg) {
        this.request = requestArg;
        this.username = usernameArg;
        this.password = passwordArg;
        return this.succeed;
    }

    /**
     * Return the value this object will return on invocation of its interface
     * method.
     * 
     * @return Returns the succeeed.
     */
    public boolean isSucceed() {
        return this.succeed;
    }

    /**
     * Set the value this object will return on invocation of its interface
     * method.
     * 
     * @param succeeed The succeeed to set.
     */
    public void setSucceed(boolean succeeed) {
        this.succeed = succeeed;
    }

    /**
     * Get the username most recently presented to the interface method.
     * 
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Get the ServletRequest most recently presented to the interface method.
     * 
     * @return Returns the request.
     */
    public ServletRequest getRequest() {
        return this.request;
    }

    /**
     * Get the username most recently presented to the interface method.
     * 
     * @return Returns the username.
     */
    public String getUsername() {
        return this.username;
    }
}
