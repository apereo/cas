/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Domain object to bind the request for an authentication to a command object. This will bind all of the CAS 2.0 parameters.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class UsernamePasswordCredentials implements Credentials {

    private static final long serialVersionUID = -8343864967200862794L;

    private String userName;

    private String password;

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String toString() {
        ToStringBuilder toStringBuilder = new ToStringBuilder(this);
        return toStringBuilder.append("userName", this.userName).toString();
    }
}